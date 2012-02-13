(ns ciid.server
  (:require [noir.server :as server]))

; This file only contains standard Noir functionality:

; Load all files in the given directory
; (this is where all the URL routes are defined).
(server/load-views "src/ciid/views/")

; This function is the main entry point of the
; project and is executed when launched with
; the "lein run" command...
(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'ciid-tweeq})))

