(ns ciid.views.common
  (:require
    [noir.session :as session]
    [ciid.dates :as dates])
  (:use
    [noir.core :only [defpartial]]
    [hiccup.page-helpers :only [include-css html5]]))

; This file only contains common/re-usable/shared
; functionality (or rather templates)

; FYI. A "partial" is a kind of incomplete template which can be
; treated like a normal function (incl. parameters) and can be
; used from within other templates...

; This partial defines & generates the HTML structure for
; a single tweet item...
(defpartial tweet-item
  [t]
  (let[{:keys [_id user text date_created]} (:doc t)]
    [:div.clearfix.tweet
     [:p text]
     [:p.meta
      [:a {:href (str "/tweets/" _id)}
       user " / " (dates/format-daytime-array date_created)]]]))

; This partial template defines the global HTML structure incl.
; top navigation bar and links to CSS files.
; Any parameters are injected as the main body of the page.
(defpartial layout [& content]
  (html5
    [:head
     [:title "Welcome to CIID Tweeq"]
     (include-css "/css/bootstrap.css")
     (include-css "/css/ciid.css")
    ]
    [:body
     [:div.topbar
      [:div.fill
        [:div.container
          [:a.brand {:href "/"} "CIID Tweeq"]
          (when (session/get :user)
            [:ul
             [:li [:a {:href "/users"} "All users"]]
             [:li [:a {:href (str "/users/" (:username (session/get :user)))} "My profile"]]
             [:li [:a {:href "/tweets/add"} "+ Add tweet"]]
             [:li [:a {:href "/triggers/add"} "+ Add trigger"]]])
          (if (session/get :user)
            [:form.pull-right {:action "/logout" :method "GET"}
             [:button.btn {:type "submit"} "Logout"]]
            [:form.pull-right {:action "/login" :method "GET"}
             [:a {:href "/users/add"} "Signup"] " "
             [:button.btn {:type "submit"} "Login"]])]]]
     [:div.container
      [:div.content content]]]))
