(ns ^{:doc "Api layer for creating atom entries  and for getting published feeds" :author "Thomas Engelschmidt"}
  feeds.atoms 
  (:use [feeds.db :as db]
        clojure-config.core
        [ring.commonrest  :only (chk is-empty?)])
  (:require [clojure.contrib.logging :as logging] [clojure.contrib.lazy-xml :as lazy ])
  (:import java.util.Calendar))

(def ^{:private true} feed-template '{:tag :feed, :attrs {:xmlns "http://www.w3.org/2005/Atom"}} )


(defn- feed-body [date] 
         `({:tag :title,  :content (~(get-property  "feed-title" )), :attrs {:type "text"}}
           {:tag :id,     :content (~(get-property "feed-uuid")),:attrs {}}
           {:tag :author, :content (~(get-property "feed-author")),:attrs {}}
           {:tag :updated,:content (~date), :attrs {}}))

(defn link "creates a link. reftype could be self, via, next, prev, prev-archive, next-archive" [ref-type uri] 
  `{:tag :link,:content "", :attrs {:ref ~ref-type :href ~uri}}) 

(defn entry-summary "" [text]
  `{:tag :summary, :content (~(str text)),:attrs {}})

(defn entry-content-text "" [text] 
  `{:tag :content, :content (~(str text)), :attrs {:type "text"}})

(defn author "" [name]
   `{:tag :author, :content (~(str name)),:attrs {}})

;(defn entry-source "" [] )

(defn entry-category "creates a category. <category term=':TERM'/>" [term]
  `{:tag :category,:content "", :attrs {:term ~term}}) 

(defn- rm-nil "Removes nil elements from a list" 
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

(defn as-atom-date "Takes an java.util.Calendar and created a date string like 2009-07-01T11:58:00Z" [cal]
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
     (let [links (first (logging/spy opts))]
         (with-out-str (lazy/emit  
                (conj feed-template 
                     {:content (rm-nil (logging/spy(concat (logging/spy(conj (feed-body timestamp)  
                                                (if (:via links) (link "via" (:via links)))
                                                (if (:prev-archive links) (link "prev-archive" (:prev-archive links)))
                                                (if (:next-archive links) (link "next-archive" (:next-archive links)))
                                                (link "self" self-uri)))
                                              entries)))}))
      :indent 2)))

(defn- parse-xml [xml] 
    (lazy/parse-trim 
      (java.io.StringReader. xml)))

(defn create-atom-entry "Builds an Atom entry with a title and optional elements" [title & elements]
      `{:tag :entry, :attrs {}, :content 
        ~(concat `( {:tag :id, :attrs {}, :content ( ~(. ( java.util.UUID/randomUUID ) toString)) } 
                  {:tag :updated, :attrs {}, :content ( ~(as-atom-date(java.util.Calendar/getInstance )))}
                  {:tag :title, :attrs {:type "text"}, :content (~title)}) 
               elements) })

(defn add-entry "In the same maps format as lazy-xml parse xml into " [feed entry] 
  (db/insert-atom-entry feed entry)) 

(defn add-xml-entry [feed xml] 
  (db/insert-atom-entry feed  
       (parse-xml xml)))


(defn find-feed "Get a feed for at given date" [feed ^Integer day ^Integer month ^Integer year]
;   {:pre [(chk 400 (and (> day 0) (< day 32)))
 ;         (chk 400 (and (> month 0) (< month 13)))
 ;         (chk 400 (> year 2009))
 ;         (chk 400 (is-empty? feed))]
   ; :post [(chk 404 (is-empty? %))]  
  ;  }
    (let [raw-cal (java.util.Calendar/getInstance)
          date {:dd day, :mm month :yy year} 
          url (get-property "feed-url")
          prev-date (db/find-prev-archive-date feed (:dd date) (:mm date) (:yy date ) )
          next-date (db/find-next-archive-date feed (:dd date) (:mm date) (:yy date ) )
          entries (db/find-atom-feed feed (:dd date) (:mm date) (:yy date ))
          self (uri-with-date url date)
          links (merge (if (not (nil? prev-date)) {:prev-archive (uri-with-date url (date-as (sqldate-to-cal prev-date)))})  
                       (if (not (nil? next-date)) {:next-archive (uri-with-date url (date-as (sqldate-to-cal next-date)))}))] 
      (create-feed (as-atom-date raw-cal) entries self links)))



(defn current-feed "Get the current feed, which is now. It can contains zero entries" [feed]
    (let [raw-cal (java.util.Calendar/getInstance)
          date (date-as raw-cal )
          url (get-property "feed-url")
          prev-date (db/find-prev-archive-date feed (:dd date) (:mm date) (:yy date ))
          entries (db/find-atom-feed feed (:dd date) (:mm date) (:yy date ))
          self (get-property "feed-current-url") ] 
      (create-feed (as-atom-date raw-cal) entries self  
                   (merge {:via (uri-with-date url  date)} 
                         (if (not (nil? prev-date)) {:prev-archive (uri-with-date url (date-as (sqldate-to-cal prev-date)))})))))

(defn find-entry "Find an atom entry under feed with id" [feed id]
    {:pre [(chk 400 (is-empty? feed))
          (chk 400 (is-empty? id))]
     :post [(chk 404 (is-empty? %))]} 
    (db/find-atom-entry feed id))
