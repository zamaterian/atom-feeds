(ns ^{:doc "Api layer for creating atom entries  and for getting published feeds" :author "Thomas Engelschmidt"}
  feeds.atoms 
  (:use [feeds.db :as db]
        clojure-config.core
        [ring.commonrest  :only (chk is-empty?)])
  (:require [clojure.contrib.lazy-xml :as lazy ]
            [clojure.contrib.logging :as logging])
  (:import java.util.Calendar))

(def ^{:private true} feed-template '{:tag :feed, :attrs {:xmlns "http://www.w3.org/2005/Atom"}} )


(defn- feed-body [date] 
         `({:tag :title,  :content (~(get-property  "feed-title" )), :attrs {:type "text"}}
           {:tag :id,     :content (~(get-property "feed-uuid")),:attrs {}}
           {:tag :author, :content (~(get-property "feed-author")),:attrs {}}
           {:tag :updated,:content (~date), :attrs {}}))

(defn link [ref-type uri] 
  `{:tag :link,:content "", :attrs {:ref ~ref-type :href ~uri}}) 

(defn-  rm-nil "Removes nil elements from a list" 
  [data] 
  (filter (fn [x] (not (empty? x))) data)) 

(defn- date-as [cal] 
  {:dd (. cal get 5) ,:mm (+ 1(. cal get 2)),:yy (. cal get 1)}) 


(defn- sqldate-to-cal [date]
    (let [cal (java.util.Calendar/getInstance)]
         (. cal setTime date )
         cal))


(defn- zero-pad [id]
  (if (< id 10) (str "0" id)  id))

(defn as-atom-date [cal]
   (str (. cal get (Calendar/YEAR)) "-"  
        (zero-pad (+ 1(. cal get (Calendar/MONTH)))) "-"
        (zero-pad (. cal get (Calendar/DATE))) "T"
        (zero-pad (. cal get (Calendar/HOUR_OF_DAY))) ":"
        (zero-pad (. cal get (Calendar/MINUTE))) ":"
        (zero-pad (. cal get (Calendar/SECOND))) "Z"))

(defn- uri-with-date [uri date]
  (str  uri (:dd date ) "/" (:mm date ) "/"(:yy date ) "/"))

(defn- create-feed "optional links can be :prev-archive 'http://xxxx-xxx' ,:next-archive ,:via "
      [timestamp entries self-uri & opts] 
     (let [links (first opts)]
         (lazy/emit 
                (conj feed-template 
                     {:content (rm-nil (concat (conj (feed-body timestamp)  
                                              (if (:via links) (link "via" (:via links)))
                                              (if (:prev-archive links) (link "prev-archive" (:prev-archive links)))
                                              (if (:next-archive links) (link "next-archive" (:next-archive links)))
                                              (link "self" self-uri))
                                              entries))})
      :indent 2)))

(defn- parse-xml [xml] 
    (lazy/parse-trim 
      (java.io.StringReader. xml)))

(defn add-xml-entry [feed xml] 
  (db/insert-atom-entry feed  
       (parse-xml xml)))


(defn find-feed "Get a feed for at given date" [feed ^Integer day ^Integer month ^Integer year]
   {:pre [(chk 400 (and (> day 0) (< day 32)))
          (chk 400 (and (> month 0) (< month 13)))
          (chk 400 (> year 2009))
          (chk 400 (is-empty? feed))]
   ; :post [(chk 404 (is-empty? %))]  
    }
    (let [raw-cal (java.util.Calendar/getInstance)
          date {:dd day, :mm month :yy year} 
          url (get-property "feed-url")
          prev-date (db/find-prev-archive-date feed (:dd date) (:mm date) (:yy date ) )
          next-date (db/find-next-archive-date feed (:dd date) (:mm date) (:yy date ) )
          entries (db/find-atom-feed feed (:dd date) (:mm date) (:yy date ))
          self (uri-with-date url date)] 
      (create-feed (as-atom-date raw-cal) entries self  
                   (conj  (if (not (nil? prev-date)) {:prev-archive (uri-with-date url (date-as (sqldate-to-cal prev-date)))})
                          (if (not (nil? next-date)) {:next-archive (uri-with-date url (date-as (sqldate-to-cal next-date)))})))))



(defn current-feed "Get the current feed, which is now. It can contains zero entries" [feed]
    (let [raw-cal (java.util.Calendar/getInstance)
          date (date-as raw-cal )
          url (get-property "feed-url")
          prev-date (db/find-prev-archive-date feed (:dd date) (:mm date) (:yy date ))
          entries (db/find-atom-feed feed (:dd date) (:mm date) (:yy date ))
          self (get-property "feed-current-url") ] 
          (prn date)   
      (create-feed (as-atom-date raw-cal) entries self  
                   (conj {:via (uri-with-date url  date)} (if (not (nil? prev-date)) {:prev-archive (uri-with-date url (date-as (sqldate-to-cal prev-date)))})))))

(defn find-entry "Find an atom entry under feed with id" [feed id]
    {:pre [(chk 400 (is-empty? feed))
          (chk 400 (is-empty? id))]
     :post [(chk 404 (is-empty? %))]} 
    (db/find-atom-entry feed id))
