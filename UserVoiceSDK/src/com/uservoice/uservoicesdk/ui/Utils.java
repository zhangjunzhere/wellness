package com.uservoice.uservoicesdk.ui;

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;

import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.SystemProperties;
import com.uservoice.uservoicesdk.activity.ArticleActivity;
import com.uservoice.uservoicesdk.activity.TopicActivity;
import com.uservoice.uservoicesdk.dialog.SuggestionDialogFragment;
import com.uservoice.uservoicesdk.model.Article;
import com.uservoice.uservoicesdk.model.BaseModel;
import com.uservoice.uservoicesdk.model.Suggestion;
import com.uservoice.uservoicesdk.model.Topic;

public class Utils {

    @SuppressLint("SetJavaScriptEnabled")
    public static void displayArticle(WebView webView, Article article, Context context) {
        String styles = "iframe, img { width: 100%; }";
        if (isDarkTheme(context)) {
            webView.setBackgroundColor(Color.BLACK);
            styles += "body { background-color: #000000; color: #F6F6F6; } a { color: #0099FF; }";
        }
        String html = String.format("<html><head><meta charset=\"utf-8\"><link rel=\"stylesheet\" type=\"text/css\" href=\"http://cdn.uservoice.com/stylesheets/vendor/typeset.css\"/><style>%s</style></head><body class=\"typeset\" style=\"font-family: sans-serif; margin: 1em\"><h3>%s</h3>%s</body></html>", styles, article.getTitle(), article.getHtml());
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(PluginState.ON);
        webView.loadUrl(String.format("data:text/html;charset=utf-8,%s", Uri.encode(html)));
    }

    public static boolean isDarkTheme(Context context) {
        TypedValue tv = new TypedValue();
        float[] hsv = new float[3];
        context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true);
        Color.colorToHSV(context.getResources().getColor(tv.resourceId), hsv);
        return hsv[2] > 0.5f;
    }

    public static boolean isSimilarToWhite(int color){
        int threshold = 30;
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return (255 - r) < threshold && (255 - g) < threshold && (255 - b) < threshold;
    }

    @SuppressLint("DefaultLocale")
    public static String getQuantityString(View view, int id, int count) {
        return String.format("%,d %s", count, view.getContext().getResources().getQuantityString(id, count));
    }

    public static void displayInstantAnswer(Context context, View view, BaseModel model) {
        TextView title = (TextView) view.findViewById(R.id.uv_title);
        TextView detail = (TextView) view.findViewById(R.id.uv_detail);
        View suggestionDetails = view.findViewById(R.id.uv_suggestion_details);
        ImageView image = (ImageView) view.findViewById(R.id.uv_icon);
        if (model instanceof Article) {
            Article article = (Article) model;
            image.setImageResource(R.drawable.uv_article);
            title.setText(article.getTitle());
            //if (article.getTopicName() != null) {
            if (false) {
                detail.setVisibility(View.VISIBLE);
                detail.setText(article.getTopicName());
            } else {
                detail.setVisibility(View.GONE);
            }
            suggestionDetails.setVisibility(View.GONE);
        } else if (model instanceof Suggestion) {
            Suggestion suggestion = (Suggestion) model;
            image.setImageResource(R.drawable.uv_idea);
            title.setText(suggestion.getTitle());
            detail.setVisibility(View.GONE);
            //detail.setText(suggestion.getForumName());
            if (suggestion.getStatus() != null) {
                View statusColor = suggestionDetails.findViewById(R.id.uv_suggestion_status_color);
                TextView status = (TextView) suggestionDetails.findViewById(R.id.uv_suggestion_status);
                int color = Color.parseColor(suggestion.getStatusColor());
                suggestionDetails.setVisibility(View.VISIBLE);
                //status.setText(suggestion.getStatus().toUpperCase(Locale.getDefault()));
                status.setText((Suggestion.getTranslationStatus(context, suggestion.getStatus())).toUpperCase(Locale.getDefault()));
                status.setTextColor(color);
                statusColor.setBackgroundColor(color);
            } else {
                suggestionDetails.setVisibility(View.GONE);
            }

        }
    }

    public static void showModel(FragmentActivity context, BaseModel model) {
        showModel(context, model, null);
    }

    public static void showModel(FragmentActivity context, BaseModel model, String deflectingType) {
        if (model instanceof Article) {
            /*
            ArticleDialogFragment fragment = new ArticleDialogFragment();
            Bundle args = new Bundle(2);
            args.putParcelable(ArticleDialogFragment.ARG_ARTICLE, (Article) model);
            args.putString(ArticleDialogFragment.ARG_DEFLECTING_TYPE, deflectingType);
            fragment.setArguments(args);
            fragment.show(context.getSupportFragmentManager(), "ArticleDialogFragment");
            */
            Intent intent = new Intent(context, ArticleActivity.class);
            ArrayList<Article> articles = new ArrayList<Article>();
            articles.add((Article) model);
            intent.putExtra(Article.class.getName(), articles);
            intent.putExtra(ArticleActivity.POSITION, 0);
            context.startActivity(intent);
        } else if (model instanceof Suggestion) {
            SuggestionDialogFragment fragment = new SuggestionDialogFragment();
            Bundle args = new Bundle(2);
            args.putParcelable(SuggestionDialogFragment.ARG_SUGGESTION, (Suggestion) model);
            args.putString(SuggestionDialogFragment.ARG_DEFLECTING_TYPE, deflectingType);
            fragment.setArguments(args);
            fragment.show(context.getSupportFragmentManager(), "SuggestionDialogFragment");
        } else if (model instanceof Topic) {
            Intent intent = new Intent(context, TopicActivity.class);
            intent.putExtra("topic", (Topic) model);
            context.startActivity(intent);
        }
    }

    public static boolean isCNSku() {
        String sku = SystemProperties.get("ro.build.asus.sku").toLowerCase();
        String productName = SystemProperties.get("ro.product.name").toLowerCase();
        String amaxSku = SystemProperties.get("ro.build.asus.oem.region").toLowerCase();
        if(amaxSku != null && amaxSku.startsWith("cn"))
            return true;
        if(sku == null || productName == null)
            return false;
        return sku.startsWith("cn") || sku.startsWith("cucc") || productName.startsWith("cn") || productName.startsWith("cucc") ? true : false;
    }

    public static void setupWindowTranslucentStatus(Activity activity) {
        if (activity.getResources().getIdentifier("windowTranslucentStatus", "attr", "android") != 0) {
            Window w = activity.getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
