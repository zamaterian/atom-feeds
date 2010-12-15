(ns ^{:doc "Api layer for creating atom entries  and for getting published feeds" :author "Thomas Engelschmidt"}
  feeds.atoms 
  (:use [feeds.db :as db]
        feeds.config
        [ring.commonrest  :only (chk is-empty?)])
  (:require [clojure.contrib.lazy-xml :as lazy ]
            [clojure.contrib.logging :as logging]))

(def ^{:private true} feed-template '{:tag :feed, :attrs {:xmlns "http://www.w3.org/2005/Atom"}} )


(defn- feed-body [date] 
         `({:tag :title,  :content (~(property  "title" )), :attrs {:type "text"}}
           {:tag :id,     :content (~(property "uuid")),:attrs {}}
           {:tag :author, :content (~(property "author")),:attrs {}}
           {:tag :updated,:content (~date), :attrs {}}))

(defn link [ref-type uri] 
  `{:tag :link,:content "", :attrs {:ref ~ref-type :href ~uri}}) 

(defn-  rm-nil "Removes nil elements from a list" 
  [data] 
  (filter (fn [x] (not (empty? x))) data)) 

(defn- date-as [cal] ; cond sqldate calendar, java.util.date 
  {:dd (. cal get 5) ,:mm (+ 1(. cal get 2)),:yy (. cal get 1)}) 

(defn- create-feed "optional links can be :prev-archive 'http://xxxx-xxx' ,:next-archive ,:via "
      [date entries self-uri & opts] 
     (let [links (first opts)]
         (lazy/emit 
                (conj feed-template 
                     {:content (rm-nil (concat (conj (feed-body date)  
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



(defn find-feed "" [feed ^Integer day ^Integer month ^Integer year]
   {:pre [(chk 400 (and (> day 0) (< day 32)))
          (chk 400 (and (> month 0) (< month 13)))
          (chk 400 (> year 2009))
          (chk 400 (is-empty? feed))]
   ; :post [(chk 404 (is-empty? %))]  because of current 
    }

   (db/find-atom-feed feed day month year)) 

(defn current-feed [feed]
    (let [raw-cal (java.util.Calendar/getInstance)
          date (date-as raw-cal )
          prev-date (db/find-prev-archive-date feed (:dd date) (:mm date) (:yy date ) )
          entries (find-feed feed (:dd date) (:mm date) (:yy date ))
          self (property "current-feed-url")] 
      (create-feed (str date) entries self  
                   (conj {:via "via-url"} (if (not (nil? prev-date)) {:prev-archive (str prev-date)})))))


(defn find-entry "Find an atom entry under feed with id" [feed id]
    {:pre [(chk 400 (is-empty? feed))
          (chk 400 (is-empty? id))]
     :post [(chk 404 (is-empty? %))]} 
    (db/find-atom-entry feed id))
