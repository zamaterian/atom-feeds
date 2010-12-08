(ns ^{:doc "Api layer for creating atom entries  and for getting published feeds" :author "Thomas Engelschmidt"}
  feeds.atoms 
  (:use feeds.db
        clojure.contrib.prxml
    [ring.commonrest :only [chk is-empty?]]))


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
              [:id "hardcode uid"] 
              [:generator {:uri "http://kunde/sso/"} "Kunde sso system"]
              [:author "kunde-interface"]
              [:months-to-live "6"]
              [:link {:ref "self" :href "http://feed/notification/current/"} ]
              [:updated "2009-07-01T11:58:00Z"]])

(defn- as-xml [data entries] 
  (with-out-str 
    (binding [*prxml-indent* 2]
      (prxml ver (conj data entries)))))

(defn build-feed [] (as-xml feed_ entries))

 (defn feed "" [feed ^Integer day ^Integer month ^Integer year]
   {:pre [(chk 400 (and (> day 0) (< day 32)))
          (chk 400 (and (> month 0) (< month 13)))
          (chk 400 (> year 2009))
          (chk 400 (is-empty? feed))]
    :post [(chk 404 (is-empty? %))]}
   (find-atom-feed feed day month year)) 

   (defn entry "Find an atom entry under feed with id" [feed id]
     {:pre [(chk 400 (is-empty? feed))
          (chk 400 (is-empty? id))]
      :post [(chk 404 (is-empty? %))]} 
     (find-atom-entry feed id))
