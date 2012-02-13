(ns ciid.config)

; This file only contains configuration settings
; used throughout the entire project...

; First off is the CouchDB database connection URL.
; Since the Amazon EC2 instance we used for the workshop
; is not available anymore, a mirrored snapshot of
; that database is located at the free IrisCouch
; CouchDB hosting service...
;
; Please update the username and password below with the
; details sent to you via email to configure this correctly:
(def db "http://user:password@ciid.iriscouch.com:5984/ciid-tweeq")

; If you want to switch to your local CouchDB instance,
; please use the connection below (just uncomment the line)
; (def db "http://localhost:5984/ciid-tweeq")

; This relates to the maximum number of recent tweets
; returned by CouchDB for the /tweets & /xml/tweets URL routes
(def max-tweets 200)

; One of these greetings is randomly used on the user
; profile pages, i.e. this URL route: /users/:id
(def greetings [
  "Hi"
  "Hej"
  "Hola"
  "Hello"
  "Hallo"
  "Bonjour"
  ])
