package jp.techacademy.tomotaka.aruga.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.tomotaka.aruga.qa_app.databinding.ActivityFavoriteBinding
import jp.techacademy.tomotaka.aruga.qa_app.databinding.ActivityMainBinding

class FavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding

    private var genre = 0

    // ----- 追加:ここから -----
    private lateinit var databaseReference: DatabaseReference
    private lateinit var questionArrayList: ArrayList<Question>
    private lateinit var adapter: QuestionsListAdapter
    private lateinit var question: Question

    private var favoriteref: DatabaseReference? = null

    // ----- 追加:ここまで -----

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.menu_favorite_label)

        binding.favoriteList.setOnItemClickListener { _, _, position, _ ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", questionArrayList[position])
            startActivity(intent)
        }


    }

    private val eventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {


            val map = snapshot.value as Map<*, *>
            val key = snapshot.key.toString()
            val genre = map["genre"] as? String ?: ""
            val favoritekeyref = databaseReference.child(ContentsPATH).child(genre).child(key)

            favoritekeyref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot2: DataSnapshot) {
                    val map2 = snapshot2.value as Map<*, *>
                    val title = map2["title"] as? String ?: ""
                    val body = map2["body"] as? String ?: ""
                    val name = map2["name"] as? String ?: ""
                    val uid = map2["uid"] as? String ?: ""
                    val imageString = map2["image"] as? String ?: ""
                    val bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }

                    val answerArrayList = ArrayList<Answer>()
                    val answerMap = map2["answers"] as Map<*, *>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val map3 = answerMap[key] as Map<*, *>
                            val map1Body = map3["body"] as? String ?: ""
                            val map1Name = map3["name"] as? String ?: ""
                            val map1Uid = map3["uid"] as? String ?: ""
                            val map1AnswerUid = key as? String ?: ""
                            val answer = Answer(map1Body, map1Name, map1Uid, map1AnswerUid)
                            answerArrayList.add(answer)
                        }
                    }

                    val question = Question(
                        title, body, name, uid, key,
                        genre.toInt(), bytes, answerArrayList
                    )
                    questionArrayList.add(question)
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


            Log.d("jjj", map["genre"].toString())
            Log.d("jjj", snapshot.key.toString())
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    }

    override fun onResume() {
        super.onResume()
        adapter = QuestionsListAdapter(this)
        questionArrayList = ArrayList()
        adapter.setQuestionArrayList(questionArrayList)
        binding.favoriteList.adapter = adapter
        adapter.notifyDataSetChanged()

        val user = FirebaseAuth.getInstance().currentUser!!.uid

        databaseReference = FirebaseDatabase.getInstance().reference

        favoriteref = databaseReference.child(FavoritesPath).child(user)
        favoriteref!!.addChildEventListener(eventListener)
    }


}



