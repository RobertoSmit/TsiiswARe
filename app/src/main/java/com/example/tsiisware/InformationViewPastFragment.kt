package com.example.tsiisware

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class InformationViewPastFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var label: String
    private lateinit var category: String
    private lateinit var webView: WebView
    private lateinit var title: TextView
    private lateinit var informationText: TextView
    private lateinit var resetButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_informationview_past, container, false)

        webView = view.findViewById(R.id.webView)
        informationText = view.findViewById(R.id.informationTextPast)
        resetButton = view.findViewById(R.id.resetVideobtn)
        title = view.findViewById(R.id.titleTextVideoPast)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.setOnTouchListener { _, _ -> true } // Disable user interaction

        resetButton.setOnClickListener {
            loadInformation()
        }

        informationText.text = "Loading..."
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        label = arguments?.getString("label") ?: ""
        category = arguments?.getString("category") ?: ""

        loadInformation()
    }

    private fun loadInformation() {
        db.collection("video_objects").document(label.lowercase()).get().addOnSuccessListener { document ->
            if (document != null) {
                val videoUrl = document.getString("video_url")
                val iframeStructure = String.format("<iframe width=\"100%%\" height=\"100%%\" src=\"https://www.youtube.com/embed/%s\" frameborder=\"0\" allowfullscreen></iframe>", videoUrl?.split("v=")!![1]);
                webView.loadData(iframeStructure, "text/html", "utf-8")
                title.text = document.getString("label")
                informationText.text = document.getString("description")
                informationText.setTextColor(Color.WHITE)
            }
        }
    }

    companion object {
        fun newInstance(label: String, category: String): InformationViewCurrentFragment {
            val fragment = InformationViewCurrentFragment()
            val args = Bundle()
            args.putString("label", label)
            args.putString("category", category)
            fragment.arguments = args
            return fragment
        }
    }
}