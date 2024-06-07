package jp.techacademy.tomotaka.aruga.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import jp.techacademy.tomotaka.aruga.qa_app.databinding.ListQuestionsBinding

class QuestionsListAdapter(context: Context) : BaseAdapter() {
    // レイアウトインフレーターのインスタンスを保持
    private var layoutInflater: LayoutInflater
    // 質問のリストを保持
    private var questionArrayList = ArrayList<Question>()

    init {
        // コンストラクタでレイアウトインフレーターを初期化
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    // リストのアイテム数を返す
    override fun getCount(): Int {
        return questionArrayList.size
    }

    // 指定された位置のアイテムを返す
    override fun getItem(position: Int): Any {
        return questionArrayList[position]
    }

    // 指定された位置のアイテムのIDを返す
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // 指定された位置のアイテムのビューを返す
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // ViewBindingを使うための設定
        val binding = if (convertView == null) {
            // convertViewがnullの場合、新しいビューをインフレート
            ListQuestionsBinding.inflate(layoutInflater, parent, false)
        } else {
            // convertViewが存在する場合、既存のビューを再利用
            ListQuestionsBinding.bind(convertView)
        }
        // convertViewがnullの場合、新しいビューを返す
        val view: View = convertView ?: binding.root

        // 質問のタイトルを設定
        binding.titleTextView.text = questionArrayList[position].title
        // 質問者の名前を設定
        binding.nameTextView.text = questionArrayList[position].name
        // 回答数を設定
        binding.resTextView.text = questionArrayList[position].answers.size.toString()

        // 質問の画像を設定
        val bytes = questionArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            binding.imageView.setImageBitmap(image)
        }

        // 完成したビューを返す
        return view
    }

    // 質問のリストを設定するメソッド
    fun setQuestionArrayList(questionArrayList: ArrayList<Question>) {
        this.questionArrayList = questionArrayList
    }
}