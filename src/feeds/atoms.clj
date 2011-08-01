(ns ^{:doc "Api layer for creating atom entries  and for getting published feeds" :author "Thomas Engelschmidt"}
  feeds.atoms 
  (:use [feeds.db :as db]
        [ring.commonrest  :only (chk is-empty?)])
  (:require [clojure.contrib.logging :as logging] [clojure.contrib.lazy-xml :as lazy ])
  (:import java.util.Calendar))

(def ^{:private true} feed-template '{:tag :feed, :attrs {:xmlns "http://www.w3.org/2005/Atom"}} )

(defn- check-property [id]
  (if (empty? id) (logging/error (str "Missing property for atoms feed. Property: " id  ))))

(def title) 
(def uuid)
(def mediatype)
(def feed-author) 
(def db)
(def feed)
(def url)
(def current-url) 
(def entries-per-feed 100) 

(defn- check-config [] 
  (check-property title)
  (check-property uuid)
  (check-property mediatype)
  (check-property feed-author)
  (check-property db)  
  (check-property feed)  
  (check-property url)
  (check-property current-url))

(defn- attibute [att attrs]
         (if (att attrs) {att (att attrs)}))

(defn link "creates a link. reftype could be self, via, next, prev, prev-archive, next-archive.
           And the following attributes (4.2.7 from RFC 4287) : :type {atomMediaType}?, :hreflang {atomLanguageTag}?, :title  {text}?, :length {text}? "
           [ref-type uri  & {:as attrs}] 
  `{:tag :link,:content "", :attrs 
      ~(merge {:ref ref-type :href uri}
         (attibute :type attrs)
         (attibute :hreflang attrs)
         (attibute :title attrs)
         (attibute :length attrs))}) 

(defn entry-summary "Entry - summary element" [text]
  `{:tag :summary, :content (~(str text)),:attrs {}})

(defn entry-content-text "Entry - content element" [text] 
  `{:tag :content, :content (~(str text)), :attrs {:type "text"}})

(defn author "Author element" [name]
   `{:tag :author, :content ( {:tag :name, :content (~(str name)),:attrs {}}),:attrs {}})

(defn feed-body [date] 
         `({:tag :title,  :content (~title ), :attrs {:type "text"}}
           {:tag :id,     :content (~uuid),:attrs {}}
           ~(author feed-author)
           {:tag :updated,:content (~date), :attrs {}}))
;(defn entry-source "" [] )

(defn entry-category "creates a category. <category term=':TERM'/>" [term]
  `{:tag :category,:content "", :attrs {:term ~term}}) 

(defn- rm-nil "Removes nil elements from a list" 
  [data] 
  (filter (fn [x] (not (empty? x))) data)) 

(defn- date-as [cal] 
  {:dd (. cal get 5) ,:mm (+ 1(. cal get 2)),:yy (. cal get 1)}) 

(defn- zero-pad [id]
  (if (< id 10) (str "0" id)  id))

(defn- as-atom-date "Takes an java.util.Calendar and created a date string like 2009-07-01T11:58:00Z" [cal]
   (str (. cal get (Calendar/YEAR)) "-"  
        (zero-pad (+ 1(. cal get (Calendar/MONTH)))) "-"
        (zero-pad (. cal get (Calendar/DATE))) "T"
        (zero-pad (. cal get (Calendar/HOUR_OF_DAY))) ":"
        (zero-pad (. cal get (Calendar/MINUTE))) ":"
        (zero-pad (. cal get (Calendar/SECOND))) "Z"))

(defn- uri-with
  [uri rank-start]
  (str uri rank-start "/"))

(defn uri-prev [base-url offset]
    (str base-url "prev/" offset "/"))

(defn uri-next [base-url offset]
  (str base-url "next/" offset "/"))


(defn- create-feed "optional links can be :prev-archive 'http://xxxx-xxx' ,:next-archive ,:via "
  [timestamp entries self-uri & opts]
  (let [links (first opts)
        type_ mediatype]
    (with-out-str (lazy/emit
                    (conj feed-template
                      {:content (rm-nil (concat (conj (feed-body timestamp)
                                                  (if (:via links) (link "via" (:via links) :type type_))
                                                  (if (:prev-archive links) (link "prev-archive" (:prev-archive links) :type type_))
                                                  (if (:next-archive links) (link "next-archive" (:next-archive links) :type type_))
                                                  (link "self" self-uri :type type_))
                                          entries))}) :indent 2))))

(defn- parse-xml [xml] 
    (lazy/parse-trim 
      (java.io.StringReader. xml)))

(defn- add-xml-entry [feed xml] 
  (db/insert-atom-entry feed (parse-xml xml) db ))

(defn create-atom-entry "Builds an Atom entry with a title and optional elements" [title & elements]
      `{:tag :entry, :attrs {}, :content 
        ~(concat `( {:tag :id, :attrs {}, :content ( ~(. ( java.util.UUID/randomUUID ) toString)) } 
                  {:tag :updated, :attrs {}, :content ( ~(as-atom-date(java.util.Calendar/getInstance )))}
                  {:tag :title, :attrs {:type "text"}, :content (~title)}) 
               elements)})

(defn add-entry "In the same maps format as lazy-xml parse xml into" 
  ([entry] 
     (db/insert-atom-entry feed entry db))
  ([feed entry date]
     (db/insert-atom-entry feed entry date db))) 

(defn- calc-next-chunk [start total]
   (let [end (+ start entries-per-feed)]
     (if (< end total) end 0)))

(defn find-feed "Get a feed for at given date 
                example on a transform-with-entry function:
                (defn extract-content [entry tag]  
                     (first (:content (first (filter (fn [x] (= tag (:tag x))) (:content  entry ))))))

                (defn transform-entry [uri ref media-type tag entry] 
                    (let [value (extract-content entry tag)] 
                           (merge entry {:content 
                                            (conj (:content entry) 
                                                  (link ref (str uri value \"/sso/\") :type media-type))})))"
  ([offset tranform-entry-with next?]
   {:pre [(number? offset)
          (< 0 offset)]}
    (check-config)
    (let [raw-cal (java.util.Calendar/getInstance)
          [min-seqno max-seqno entries] (db/find-atom-feed-with-offset feed offset entries-per-feed tranform-entry-with db next?)
          prev-offset (if (< 1 min-seqno) min-seqno)
          next-offset max-seqno ;TODO: Der skal findes ud af om der er flere i basen efter max-seqno
          self (uri-with url offset)
          links (merge (if prev-offset {:prev-archive (uri-prev url prev-offset)})
                       (if next-offset {:next-archive (uri-next url next-offset)}))]
      {:data (create-feed (as-atom-date raw-cal) entries self links) :cacheable? false}))

  ([tranform-entry-with]
    (check-config)
    (let [raw-cal (java.util.Calendar/getInstance)
          [prev-offset via-seqno entries] (db/find-atom-feed-newest feed entries-per-feed tranform-entry-with db)
          prev-offset  (if (< 1 prev-offset) prev-offset)
          self current-url]
      {:data (create-feed (as-atom-date raw-cal) entries self  
               (merge {:via (uri-next url via-seqno)}
                 (if prev-offset {:prev-archive (uri-prev url prev-offset)})))
       :cacheable? false})))

(defn find-entry "Find an atom entry under feed with id" [feed id]
    {:pre [(chk 400 (is-empty? feed))
          (chk 400 (is-empty? id))]
     :post [(chk 404 (is-empty? %))]} 
    (db/find-atom-entry feed id db))
