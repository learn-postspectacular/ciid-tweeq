(ns ciid.dates
  (:require
    [clj-time.core :as cljt]
    [clj-time.coerce :as cljtc]
    [clj-time.format :as cljtf]))

; Defines a formatting pattern for displaying dates
(def formatter (cljtf/formatter "yyyy-MM-dd HH:mm:ss"))

(defn array-to-daytime
  "Converts a date vector [year month day hour mins secs] into
  a proper Java DateTime object (e.g. for pretty printing)."
  [d]
  (cljt/date-time (d 0) (d 1) (d 2) (d 3) (d 4) (d 5)))

(defn daytime-to-array
  "Converts a Java DataTime object into a date vector as above
  (and as used by our project for storing dates on CouchDB)." 
  [t]
  (vec (map (fn [f] (f t)) [cljt/year cljt/month cljt/day cljt/hour cljt/minute cljt/sec])))

(defn timestamp-array
  "Returns the current date as vector: [year month day hour mins secs]"
  []
  (daytime-to-array (cljt/now)))

(defn format-daytime-array
  "Converts a DayTime instance into string either using the
  default formatter or the one specified as 2nd argument.
  If the formatter arg is a keyword it is used as key in
  clj-time's map of predefined date formatters."
  ([d]
    (format-daytime-array d formatter))
  ([d f]
    (if (keyword? f)
      (cljtf/unparse
        (get cljtf/formatters f)
        (array-to-daytime d))
      (cljtf/unparse
        f
        (array-to-daytime d)))))
