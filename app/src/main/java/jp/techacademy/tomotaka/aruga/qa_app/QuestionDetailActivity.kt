package jp.techacademy.tomotaka.aruga.qa_app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.tomotaka.aruga.qa_app.databinding.ActivityQuestionDetailBinding


class QuestionDetailActivity : AppCompatActivity(), DatabaseReference.CompletionListener {

    private lateinit var binding: ActivityQuestionDetailBinding
    private lateinit var question: Question
    private lateinit var adapter: QuestionDetailListAdapter
    private lateinit var answerRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth


    private val eventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in question.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            question.answers.add(answer)
            adapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 渡ってきたQuestionのオブジェクトを保持する
        // API33以上でgetSerializableExtra(key)が非推奨となったため処理を分岐
        @Suppress("UNCHECKED_CAST", "DEPRECATION", "DEPRECATED_SYNTAX_WITH_DEFINITELY_NOT_NULL")
        question = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("question", Question::class.java)!!
        else
            intent.getSerializableExtra("question") as? Question!!

        title = question.title

        // ListViewの準備
        adapter = QuestionDetailListAdapter(this, question)
        binding.listView.adapter = adapter
        adapter.notifyDataSetChanged()

        binding.fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", question)
                startActivity(intent)
                // --- ここまで ---
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        answerRef = dataBaseReference.child(ContentsPATH).child(question.genre.toString())
            .child(question.questionUid).child(AnswersPATH)
        answerRef.addChildEventListener(eventListener)


        val dataBase = FirebaseDatabase.getInstance().reference

        val activeuserid = FirebaseAuth.getInstance().currentUser

        if (activeuserid != null) {
            val user = FirebaseAuth.getInstance().currentUser!!.uid

            val favoriteRef =
                dataBase.child(FavoritesPath).child(user).child(question.questionUid)

            binding.favoriteImageView.setOnClickListener {
                //リファレンスに対して、オフラインキャッシュの同期を無効にするためのメソッドです。これにより、指定されたリファレンスのデータはオフライン時に自動的に同期されなくなります。
                favoriteRef.keepSynced(false)

                favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = snapshot.value as Map<*, *>?
                        favoriteprocess(favoriteRef, data == null)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@QuestionDetailActivity,
                            "データの処理に失敗しました。再試行してください。",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
            starchange()
        } else {

            binding.favoriteImageView.visibility = View.INVISIBLE

        }

    }

    private fun favoriteprocess(favoriteref: DatabaseReference, flg: Boolean) {
        if (flg) {
            //お気に入り登録処理

            val data = HashMap<String, String>()

            val genre = question.genre

            data["genre"] = genre.toString()

            favoriteref.setValue(data, this)
            binding.favoriteImageView.setImageResource(R.drawable.ic_star)

        } else {
            //お気に入り削除処理
            favoriteref.removeValue()
            binding.favoriteImageView.setImageResource(R.drawable.ic_star_border)
        }
    }

    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
//        binding.progressBar.visibility = View.GONE

        if (databaseError == null) {
//            finish()
        } else {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.question_send_error_message),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun starchange() {
        val dataBase = FirebaseDatabase.getInstance().reference
        val user = FirebaseAuth.getInstance().currentUser!!.uid
        val favoriteRef =
            dataBase.child(FavoritesPath).child(user).child(question.questionUid)
        favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as Map<*, *>?
                binding.favoriteImageView.setImageResource(if (data == null) R.drawable.ic_star_border else R.drawable.ic_star)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@QuestionDetailActivity,
                    "データの処理に失敗しました。再試行してください。",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

    }

}