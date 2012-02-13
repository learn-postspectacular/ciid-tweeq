(ns ciid.views.users
  (:require
    [ciid.views.common :as common]
    [ciid.config :as config]
    [ciid.dates :as dates]
    [com.ashafa.clutch :as couch]
    [noir.session :as session]
    [noir.response :as response])
  (:use
    [noir.core :only [defpage render]]
    [hiccup.core :only [html]]))

; This file contains all URL handlers for dealing with user specific actions.

; This function produces a list of all registered users.
; The user list is obtained from the "all-users" CouchDB view.
; Using the map function, for each user a link to his/her user profile page is created.
(defpage "/users" []
  (let[users (couch/get-view
               config/db
               "main" "all-users"
               {:include_docs true})]
    (common/layout
      [:h1 "All users"]
      [:ul
       (map
         (fn[u]
           (let[{:keys[username fname lname]} (:doc u)]
             [:li [:a {:href (str "/users/" username)} fname " " lname] " (" username ")"]))
         users)])))

; This constitutes a user's profile page, using a parametric URL pattern.
; The regular expression ensures the last part of the URL only contains
; alphanumeric characters 
(defpage [:get ["/users/:user-id" :user #"\w+"]]
  {:keys [user-id]}
  ; first check if a user with that username exists in CouchDB...
  (if-let[user (couch/get-document
                 config/db (str "u-" user-id))]
    ; if so, then pick out all essential user details
    ; and other related bits of data (e.g. user's tweets)
    (let[{:keys [fname lname username]} user
         me (session/get :user)
         greeting (nth config/greetings (rand-int (count config/greetings)))
         tweets (couch/get-view
                  config/db
                  "main" "tweets-by-user"
                  {:startkey (vec (cons username (dates/timestamp-array)))
                   :endkey [username 1970]
                   :include_docs true
                   :descending true
                   :limit config/max-tweets})
         tweets (map common/tweet-item tweets)]
      ; Slightly vary page template depending if user to display
      ; is the same as user logged on... 
      (if (= (:username me) (:username user))
        (common/layout
          [:div.row.columns
           [:h1 greeting ", " fname " " lname]
           [:div.span8 tweets]
           [:div.span7
            [:p "Here we could show some further details/statistics and/or a list of the user's triggers..."]]])
        (common/layout
          [:div.row
           [:h1 fname " " lname "'s tweets..."]
           tweets])))))

; Skeleton for displaying/managing a list of action triggers for a user
(defpage [:get ["/users/:user-id/triggers" :user-id #"\w+"]]
  {:keys [user-id]}
  (common/layout
    [:h1 "All triggers for user: " user-id]))

; Skeleton for displaying/managing a user's single action trigger
; This could involve some statistics too (i.e. how many times the
; trigger has fired)...
(defpage [:get ["/users/:user-id/triggers/:name" :user-id #"\w+" :name #"\w+"]]
  {:keys [user-id name]}
  (common/layout
    [:h1 "Single trigger '" name "' for user: " user-id]))

; Display user login form
(defpage "/login" []
  (common/layout
    [:div.row
    [:form.form-stacked {:action "/login" :method "POST"}
     [:div.clearfix
      [:label {:for "username"} "Your username:"]
      [:input {:type "text" :id "username" :name "username"}]]
     [:div.clearfix
      [:label {:for "password"} "Your password:"]
      [:input {:type "password" :id "password" :name "password"}]]
     [:div.clearfix
      [:input.btn.primary {:type "submit" :value "login"}]]]]))

; Process a submitted user login form and authenticate with CouchDB.
; If the details are all correct, store the user information in
; the session data to allow other URL handlers verify the user is
; logged in and access user details without having to ask CouchDB again.
(defpage [:post "/login"]
  {:keys [username password]}
  ; correct user, but check password
  (if-let [user (couch/get-document config/db (str "u-" username))]
    (if (= (:password user) password)
      ; password ok, authenticate...
      (do
        (session/put! :user user)
        (response/redirect (str "/users/" (:username user))))
      ; password wrong
      (response/redirect "/login"))
    ; username wrong (i.e. no document found for that username)
    ; TODO we should really provide some error description to the user
    ; See the Noir tutorial and API how to do that using the concept
    ; of read-once flash messages...
    (response/redirect "/login")))

; Hitting this URL, all session data will be cleared and hence
; logout the user before redirecting her back to the homepage...
(defpage "/logout" []
  (session/clear!)
  (response/redirect "/"))

; Display the user signup form. This function also is a good
; candidate for refactoring and employing the DRY principle
; to reduce the amount of repetitive code for creating form elements...
; E.g. by creating a partial and injecting dynamic properties
(defpage "/users/add" []
  (common/layout
    [:div.row
    [:form.form-stacked {:action "/users/add" :method "POST"}
     [:div.clearfix
      [:label {:for "username"} "Select Username"]
      [:input {:type "text" :id "username" :name "username"}]]
     [:div.clearfix
      [:label {:for "fname"} "Enter your first name:"]
      [:input {:type "text" :id "fname" :name "fname"}]]
     [:div.clearfix
      [:label {:for "lname"} "Enter your last name:"]
      [:input {:type "text" :id "lname" :name "lname"}]]
     [:div.clearfix
      [:label {:for "password"} "Select your password"]
      [:input {:type "password" :id "password" :name "pass"}]]
     [:div.clearfix
      [:label {:for "email"} "Enter your email:"]
      [:input {:type "text" :id "email" :name "email"}]]
     [:div.clearfix
      [:input.btn.primary {:type "submit" :value "Add user"}]]]]))

; Here we receive a submitted user signup form and attempt to
; create a new user in CouchDB. First we make sure though that
; username *doesn't* already exist. If it does, then just
; redirect to the signup form (and again: would need some error message)
(defpage [:post "/users/add"]
  {:keys [username fname lname email pass]}
  (if (nil? (couch/get-document config/db (str "u-" username)))
    (let[user
         (couch/put-document
           config/db
           {:_id (str "u-" username)
            :type "user"
            :username username
            :password pass
            :fname fname
            :lname lname
            :email email})]
      (session/put! :user user)
      (response/redirect
        (str "/users/" (:username user))))
    (render "/users/add")))
