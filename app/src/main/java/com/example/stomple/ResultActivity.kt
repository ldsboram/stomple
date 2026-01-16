// ResultActivity.kt
package com.example.stomple

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class ResultActivity : AppCompatActivity() {
    // UI 요소
    private lateinit var resultTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var userIdEditText: EditText
    private lateinit var submitScoreButton: Button
    private lateinit var rankingListView: ListView
    private lateinit var mainMenuButton: Button
    private lateinit var loginButton: LoginButton

    // 게임 결과 및 점수
    private lateinit var outcome: String
    private lateinit var scoreDetails: String
    private var totalScore: Int = 0

    // 서버 URL 및 Gson 인스턴스
    private val serverUrl = "http://15.164.171.147/submit_score"
    private val gson = Gson()

    // Facebook 관련 변수
    private lateinit var callbackManager: CallbackManager
    private lateinit var accessTokenTracker: AccessTokenTracker

    // SharedPreferences 키
    private val PREFS_NAME = "user_prefs"
    private val KEY_USER_NAME = "user_name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Facebook SDK 초기화
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)

        // 액션바 숨기기
        supportActionBar?.hide()

        // 레이아웃 설정
        setContentView(R.layout.activity_result)

        // UI 요소 초기화
        resultTextView = findViewById(R.id.resultTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        userIdEditText = findViewById(R.id.userIdEditText)
        submitScoreButton = findViewById(R.id.submitScoreButton)
        rankingListView = findViewById(R.id.rankingListView)
        mainMenuButton = findViewById(R.id.mainMenuButton)
        loginButton = findViewById(R.id.login_button)

        // Intent에서 게임 결과 및 점수 정보 가져오기
        outcome = intent.getStringExtra("outcome") ?: "결과 없음"
        scoreDetails = intent.getStringExtra("scoreDetails") ?: "점수 없음"

        // UI에 결과 및 점수 설정
        resultTextView.text = "<게임 결과>\n$outcome"
        scoreTextView.text = scoreDetails

        // 총 점수 추출
        totalScore = extractTotalScore(scoreDetails)

        // SharedPreferences 초기화
        val sharedPreferences: SharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // SharedPreferences에서 저장된 이름 복원
        val savedName = sharedPreferences.getString(KEY_USER_NAME, "")
        if (!savedName.isNullOrBlank()) {
            userIdEditText.setText(savedName)
        }

        // Facebook CallbackManager 초기화
        callbackManager = CallbackManager.Factory.create()
        // Facebook 로그인 버튼에 퍼미션 설정 (vararg로 직접 전달)
        loginButton.setPermissions("public_profile")

        // Facebook 로그인 콜백 설정
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                // 사용자 정보 요청
                val request = GraphRequest.newMeRequest(loginResult.accessToken) { jsonObject, _ ->
                    try {
                        val name = jsonObject!!.getString("name")
                        userIdEditText.setText(name)

                        // 이름을 SharedPreferences에 저장
                        with(sharedPreferences.edit()) {
                            putString(KEY_USER_NAME, name)
                            apply()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@ResultActivity, "사용자 이름을 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "name")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                Toast.makeText(this@ResultActivity, "Facebook 로그인 취소", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@ResultActivity, "Facebook 로그인 오류: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // AccessTokenTracker 초기화 및 로그인 상태 변경 감지
        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(oldToken: AccessToken?, currentToken: AccessToken?) {
                if (currentToken == null) {
                    // 로그아웃된 상태
                    runOnUiThread {
                        userIdEditText.setText("")

                        // SharedPreferences에서 저장된 이름 제거
                        with(sharedPreferences.edit()) {
                            remove(KEY_USER_NAME)
                            apply()
                        }

                        Toast.makeText(this@ResultActivity, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 로그인된 상태 (필요 시 추가 로직 구현 가능)
                }
            }
        }

        // 점수 제출 버튼 클릭 리스너
        submitScoreButton.setOnClickListener {
            val userId = userIdEditText.text.toString()
            if (AccessToken.getCurrentAccessToken() == null || userId.isBlank()) {
                Toast.makeText(this, "Facebook에 로그인 후 시도해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                submitScore(userId, totalScore)
            }
        }

        // 메인 메뉴로 돌아가기 버튼 클릭 리스너
        mainMenuButton.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Facebook CallbackManager에 결과 전달
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        // AccessTokenTracker 정리
        accessTokenTracker.stopTracking()
    }

    /**
     * 점수 상세 정보에서 총 점수를 추출하는 함수
     */
    private fun extractTotalScore(scoreDetails: String): Int {
        val regex = Regex("합계: (-?\\d+) 점")
        val matchResult = regex.find(scoreDetails)
        return matchResult?.groupValues?.get(1)?.toInt() ?: 0
    }

    /**
     * 랭킹 서버에 점수를 제출하는 함수
     */
    private fun submitScore(userId: String, score: Int) {
        val client = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .build()

        // JSON 데이터 생성
        val jsonObject = JsonObject().apply {
            addProperty("user_id", userId)
            addProperty("score", score)
        }

        val requestBody = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        // 비동기로 요청 실행
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ResultActivity, "점수 전송 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        runOnUiThread {
                            displayRanking(responseBody)
                            submitScoreButton.text = "완료!"
                            submitScoreButton.isEnabled = false
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ResultActivity, "서버 오류: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    /**
     * 서버로부터 받은 랭킹 데이터를 UI에 표시하는 함수
     */
    private fun displayRanking(responseBody: String) {
        try {
            val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
            val rankingArray = jsonObject.getAsJsonArray("ranking")

            // 랭킹 데이터 목록 생성
            val rankingList = mutableListOf<String>()
            var targetPosition = -1 // 타겟 항목의 위치를 저장할 변수

            rankingArray.forEachIndexed { index, element ->
                val rankObject = element.asJsonObject
                val rank = rankObject.get("rank").asInt
                val userId = rankObject.get("user_id").asString
                val score = rankObject.get("score").asInt

                // 사용자의 아이디와 점수가 일치하는 경우 위치 저장
                if (userId == userIdEditText.text.toString() && score == totalScore) {
                    targetPosition = index
                }

                // 랭킹 목록에 추가
                rankingList.add("$rank 위, $userId : $score 점")
            }

            // ListView에 어댑터 설정
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, rankingList)
            rankingListView.adapter = adapter

            // 특정 위치로 스크롤
            if (targetPosition != -1) {
                rankingListView.setSelection(targetPosition)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "랭킹 데이터 파싱 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
