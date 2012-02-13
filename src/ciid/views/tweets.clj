(ns ciid.views.tweets
  (:require
    [ciid.views.common :as common]
    [ciid.config :as config]
    [ciid.dates :as dates]
    [clj-time.coerce :as cljtc]
    [com.ashafa.clutch :as couch]
    [noir.session :as session]
    [noir.response :as response])
  (:use
    [noir.core :only [defpage defpartial]]
    [hiccup.core :only [html escape-html]]))

; This file contains all URL handlers for dealing with tweets.

; This route displays the X most recent tweets in CouchDB.
; The tweets are produced by the "all-tweets" DB view
; and configuring it to return the results reverse chronologically
; using a flipped order for start & end keys.
; setting "include_docs" to true also forces the CouchDB to return
; the full tweet documents for each view result

; TODO add pagination to allow browsing of older tweets and reduce
; amount of tweets loaded at once...
(defpage "/tweets" []
  (let[tweets (couch/get-view
                config/db
                "main" "all-tweets"
                {:startkey (dates/timestamp-array)
                 :endkey [1970]
                 :include_docs true
                 :descending true
                 :limit config/max-tweets})]
    (common/layout
      [:h1 (count tweets) " recent public tweets"]
      ; here we make use of the tweet-item function
      ; to produce the HTML structure for each single tweet 
      (map common/tweet-item tweets))))

; This route simply shows the form for entering a new tweet...
; When submitted it will be sent to the HTTP POST version defined below 
(defpage "/tweets/add" []
  (common/layout
    [:form.form-stacked {:action "/tweets/add" :method "POST"}
     [:div.clearfix
      [:label {:for "text"} "Compose new message..."]
      [:textarea.span14 {:rows 3 :id "text" :name "text"}]]
     [:div.clearfix
      [:input.btn.primary {:type "submit" :value "Tweet!"}]]]))

; This function/URL is receiving a submitted tweet and adds it to the
; database. Before this function is executed the pre-route defined in
; access.clj is run to ensure a user is logged in. After the tweet
; has been added to the database, the user is redirected to the page
; displaying the single new tweet... (next function below)
(defpage [:post "/tweets/add"]
  {:keys [text]}
  (let[tweet
       (couch/put-document
         config/db
         {:type "tweet"
          :user (:username (session/get :user))
          :text text
          :date_created (dates/timestamp-array)})]
    (response/redirect
      (str "/tweets/" (:_id tweet)))))

; This is a parametric URL pattern used to display a single tweet.
; The regular expression defined for the :id component of the URL
; ensures this route is only fired if the ID is a 32 digit hex number...
(defpage [:get ["/tweets/:id" :id #"[0-9a-f]{32,32}"]]
  {:keys [id]}
  ; Only produce content if a document with the given ID
  ; is actually existing in CouchDB.
  ; If not a generic 404 error page is shown (because this function
  ; produced no output...)
  (if-let[tweet (couch/get-document config/db id)]
    (let[{:keys [text user date_created]} tweet]
      (common/layout
        [:div
         "On " (dates/format-daytime-array date_created) " "
         [:a {:href (str "/users/" (:user tweet))} (:user tweet)]
         " said..."]
        [:div.tweet-xl (:text tweet)]))))

; This partial template defines the XML structure for a single tweet.
; The tweet's date is converted into the Unix epoch format and the
; tweet's text body is encoded to ensure valid XML is produced...
(defpartial make-tweet-xml
  [t]
  (let[{:keys[_id date_created text user]} (:doc t)
       date_created (dates/array-to-daytime date_created)
       date_created (cljtc/to-long date_created)]
    [:tweet
     [:date date_created]
     [:text (escape-html text)]
     [:user user]
     [:id _id]]))

; This URL is the equivalent of the /tweets route but produces
; raw XML output instead of the default HTML. This route is primarily
; intended for interacting with other software, i.e. to produce the
; simple visualization in the ciid-tweeq-viz sister project.
(defpage "/xml/tweets" []
  (let[tweets (couch/get-view
                config/db
                "main" "all-tweets"
                {:startkey (dates/timestamp-array)
                 :endkey [1970]
                 :include_docs true
                 :descending true
                 :limit config/max-tweets})
       xml (reduce str (map make-tweet-xml tweets))]
    ; add custom HTTP header to define content type & text encoding format
    {:headers {"Content-Type" "application/xml; charset=utf-8"}
     :body (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?><tweets>" xml "</tweets>")}))
