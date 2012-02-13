(ns ciid.views.access
  (:require
    [noir.session :as session]
    [noir.response :as response])
  (:use
    [noir.core :only [defpage pre-route]]
    [hiccup.page-helpers :only [include-css html5]]))

; Redirect to /tweets if no URL route has been given
; (i.e. we're using /tweets as the homepage of our webapp)
(defpage "/" {}
  (response/redirect "/tweets"))

; Below are some security measures for posting new tweets
; pre-routes are checked before any of the normal (defpage)
; definitions and in this case are used to redirect the
; user to the login page if he/she is not logged in already
; We're doing this for both HTTP GET & POST versions...
(pre-route [:get "/tweets/add"] {}
  (when-not (session/get :user)
    (response/redirect "/login")))

(pre-route [:post "/tweets/add"] {}
  (when-not (session/get :user)
    (response/redirect "/login")))

