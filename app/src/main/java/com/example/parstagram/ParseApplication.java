package com.example.parstagram;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Register parse models
        ParseObject.registerSubclass(Post.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("147P3MdkbjgnV4yyBlXVsQQWagexjacjtqNCAZP1")
                .clientKey("mubO0GsuRSV4Zh8RjEFHC9gHib0tfCNiky9z7Lg7")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }


}
