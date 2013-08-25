lydia
=====

Android front end for in car use.  Currently it has only been tested on a Nexus 7 first gen.   

"lydia" is the tablet portion.
"lydia_phone" is the phone portion.  

Between the two of them there is bluetooth support for sending/receiving SMS's, media and volume control, with more to come in the future.

If you download the APK everything should work (minus the bugs ;) ).  If you plan on compiling from source there are a couple of API keys you will need to sign up for, they are all free.
Once you have signed up and gotten your keys, you will need to edit the "keys.example.xml" file and rename it to "keys.xml" in the "/res/values" folder.

First is Google if you want the navigation to work.

- Go to the API console https://code.google.com/apis/console/  
- Under the "Services" tab you will need to enable "Google Maps Androie API v2" and "Places API"
- Under the "API Access" tab you will need to "Create a new Android key" and "Create a new Browser key".  The Android key will require the SHA1 has from the keystore.  The Browser key can be left blank for referers.

Second is last.fm
- Go to http://www.last.fm/api and at the top click "Your API Accounts"
- Add a new API account, fill in the required info and click "Create Account"
- The next page has two pieces you need.  "API Key" and "Secret"

Third is Bugsense
- Go go https://www.bugsense.com and sign up.  You can use your existing Google account
- Once signed up you wil get your API key