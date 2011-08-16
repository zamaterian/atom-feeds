(ns ^{:doc "Database layer for atom feeds" :author "Thomas Engelschmidt"}
  feeds.db
  (:require [clojure.contrib.sql :as sql]
            [clojure.contrib.logging :as logging]))

(defmacro log-time
  "Evaluates expr and logs the time it took. Returns the value of
 expr."
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
      (logging/debug (str "Elapsed time: " (/ (double (- (. System (nanoTime)) start#)) 1000000.0) " msecs. s-form: " '~expr ))
     ret#))


(defn- clob-to-string [clob]
      "Turn a Derby 10.6.1.0 EmbedClob into a String"
      (with-open [rdr (java.io.BufferedReader. (.getCharacterStream clob))]
            (apply str (line-seq rdr))))
 
(defn find-atom-entry [feed id db] 
  (sql/with-connection db
    (sql/transaction                        
     (sql/with-query-results rs ["select id, feed, created_at, atom from atoms" ]  
        (conj  (first rs) {:atom (clob-to-string (:atom (first  rs)))} )))))

(defmacro transform [entry func]
  (if (nil? func) entry `(~func ~entry)))  

(def sql-feed-newest
  "select * from (
   select id, feed, dbms_lob.substr( atom, 4000, 1) as atom, created_at, seqno from atoms where feed = ?  order by seqno desc )
   where rownum <= ?")

(def sql-feed-next
   "select * from (
      SELECT id, feed, dbms_lob.substr( atom, 4000, 1) as atom, created_at, seqno FROM atoms
      WHERE seqno > ? AND feed = ?
      ORDER BY seqno ASC)
    where rownum <= ?
    ORDER BY seqno DESC")

(def sql-feed-prev
   "select * from (
      SELECT id, feed, dbms_lob.substr( atom, 4000, 1) as atom, created_at, seqno FROM atoms
      WHERE seqno < ? AND feed = ?
      ORDER BY seqno DESC)
    where rownum <= ?")

(def sql-max-seqno
   "select count(*) as maxseqno FROM (
      SELECT feed, seqno FROM atoms
      WHERE seqno > ? AND feed = ?
      ORDER BY seqno DESC)
    where rownum <= ?")


(defn- transform-map [res merge-into-entry]
  (if (fn? merge-into-entry) 
      (doall (map  (fn [x] (transform (load-string (str "'" (:atom x))) merge-into-entry )) res))
      (doall (map  #(load-string (str "'" (:atom %))) res))))
                               
(defn find-atom-feed-newest [feed amount merge-into-entry db]
  (logging/debug (str "find-atom-feed-newest args:" feed " " amount " " merge-into-entry " " db))
  (sql/with-connection db
    (let [db-res (log-time (sql/with-query-results rs [sql-feed-newest feed (+ amount 1)] (vec rs)))
          res (if (< 0 (count db-res) )(pop db-res) nil)
          via-seqno (:seqno (last db-res))
          prev-seqno (if (and (not (nil? res)) (< 1 (:seqno (last res)))) (:seqno (last res)))
          trans-res (transform-map res merge-into-entry)]
      [prev-seqno via-seqno trans-res])))

(defn find-atom-feed-with-offset [feed seqno amount merge-into-entry db next?]
  (logging/debug (str "find-atom-feed-with-offset args:" feed " " seqno " " amount " " merge-into-entry " " db " " next?))
  (sql/with-connection db
    (let [res (log-time(sql/with-query-results rs [(if next? sql-feed-next sql-feed-prev) seqno feed amount] (vec rs)))
          next-seqno (if next?
                       (if (>= (count res) amount)
                         (if (< 0 (log-time(sql/with-query-results rs [sql-max-seqno (:seqno (first res)) feed amount] (:maxseqno (first (vec rs))))))
                           (:seqno (first res))))
                       (:seqno (first res)))
          prev-seqno (if-let [last-seqno (:seqno (last res))]
                       (if (< 1 last-seqno) last-seqno))
          trans-res (transform-map res merge-into-entry)]
      [prev-seqno next-seqno trans-res])))
  
(defn- find-uuid [data] 
   (first (:content 
             (first (filter
                       (fn [x] (= :id (:tag x))) 
                       (:content data))))))

(defn insert-atom-entry
     "Insert data into the table"
   ([feed  atom_ db]
   (sql/with-connection db
      (sql/insert-values
          :atoms
          [:id :feed :atom]
          [(find-uuid atom_) feed (str atom_)])))
  ([feed atom_ date db]
   (sql/with-connection db
      (sql/insert-values
          :atoms
          [:id :feed :atom :created_at]
          [(find-uuid atom_) feed (str atom_) date]))))

(defn archive-count [feed db] 
    (log-time (sql/with-connection db
                (sql/with-query-results rs 
                   ["select count(*) as count from atoms where feed =  ?" feed] 
                      (:count  (first (vec rs)))))))
