(ns ^{:doc "Api layer for creating atom entries  and for getting published feeds" :author "Thomas Engelschmidt"}
  feeds.atoms 
  (:use [feeds.db :as db]
        [ring.commonrest  :only (chk is-empty?)])
  (:require [clojure.contrib.lazy-xml :as lazy ]
            [clojure.contrib.logging :as logging]))

(def entries '( 
              
              [:entry 
              [:id "id1"]
              [:title {:type "text"} "Test entry"] 
              [:updated "2009-07-01T11:58:00Z"]]
              [:entry 
              [:id "id2"]
              [:title {:type "text"} "Test entry"] 
              [:updated "2009-07-01T11:58:00Z"]]
              [:entry 
              [:id "id3"]
              [:title {:type "text"} "Test entry"] 
              [:updated "2009-07-01T11:58:00Z"]]
              ))

(def ver [:decl! {:version "1.0"}]) 

(def feed_  [:feed {:xmlns "http://www.w3.org/2005/Atom"} 
              [:title {:type "text"} "Test Feed"] 
              [:id "f7e35522-2c32-414a-ae83-2d77f8a2aafa"] 
              [:generator {:uri "http://kunde/sso/"} "Kunde sso system"]
              [:author "kunde-interface"]
              [:months-to-live "6"]
              [:link {:ref "self" :href "http://feed/notification/current/"} ]
              [:updated "2009-07-01T11:58:00Z"]])

(comment defn- as-xml [data entries] 
  (with-out-str 
    (binding [*prxml-indent* 2]
      (prxml ver (conj data entries)))))


;(defn build-feed [] (as-xml feed_ entries))


(defn parse-xml [xml] 
    (lazy/parse-trim 
      (java.io.StringReader. xml)))

(defn- emit-xml [data] 
  (lazy/emit data :indent 2))


(defn add-xml-entry [feed xml] 
  (db/insert-atom-entry feed  
       (parse-xml xml)))

 (defn feed "" [feed ^Integer day ^Integer month ^Integer year]
   {:pre [(chk 400 (and (> day 0) (< day 32)))
          (chk 400 (and (> month 0) (< month 13)))
          (chk 400 (> year 2009))
          (chk 400 (is-empty? feed))]
    :post [(chk 404 (is-empty? %))]}
   (db/find-atom-feed feed day month year)) 

   (defn entry "Find an atom entry under feed with id" [feed id]
     {:pre [(chk 400 (is-empty? feed))
          (chk 400 (is-empty? id))]
      :post [(chk 404 (is-empty? %))]} 
     (db/find-atom-entry feed id))
