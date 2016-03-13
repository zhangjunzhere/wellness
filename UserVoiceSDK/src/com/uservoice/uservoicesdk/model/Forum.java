package com.uservoice.uservoicesdk.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.uservoice.uservoicesdk.rest.Callback;
import com.uservoice.uservoicesdk.rest.RestTaskCallback;

public class Forum extends BaseModel {
    private String name;
    private int numberOfVotesAllowed;
    private int numberOfOpenSuggestions;
    private List<Category> categories;

    public static void loadForum(int forumId, final Callback<Forum> callback) {
        doGet(apiPath("/forums/%d.json", forumId), new RestTaskCallback(callback) {
            @Override
            public void onComplete(JSONObject object) throws JSONException {
                callback.onModel(deserializeObject(object, "forum", Forum.class));
            }
        });
    }

    public static void loadForumSuggestionCounter(int forumId, final Callback<Integer> callback) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("per_page", "1");
        doGet(apiPath("/forums/%d/suggestions.json", forumId), params, new RestTaskCallback(callback) {
            @Override
            public void onComplete(JSONObject object) throws JSONException {
                int value = object.getJSONObject("response_data").getInt("total_records");
                callback.onModel(value);
            }
        });
    }

    @Override
    public void load(JSONObject object) throws JSONException {
        super.load(object);
        name = getString(object, "name");
        JSONObject topic = object.getJSONArray("topics").getJSONObject(0);
        numberOfOpenSuggestions = topic.getInt("open_suggestions_count");
        numberOfVotesAllowed = topic.getInt("votes_allowed");
        categories = deserializeList(topic, "categories", Category.class);
        if (categories == null)
            categories = new ArrayList<Category>();
    }

    public String getName() {
        return name;
    }

    public int getNumberOfOpenSuggestions() {
        return numberOfOpenSuggestions;
    }

    public int getNumberOfVotesAllowed() {
        return numberOfVotesAllowed;
    }

    public List<Category> getCategories() {
        return categories;
    }
}