(ns unit.db 
    (:use     derby [feeds.db :only (find-atom-entry find-atom-feed) ]
              lazytest.context.stub
              [lazytest.context :only (fn-context)]
              [lazytest.describe :only (describe it given do-it for-any with before)]
              [lazytest.expect :only (expect)]
              [lazytest.expect.thrown :only (throws?)]))


(def  date (java.util.Calendar/getInstance ) )


(describe "Select atom entries"
              (with [(before (init-derby))]
                  (it "finds one entry with feed = sso and id 1"
                      (= 1 (:id (find-atom-entry "sso" 1 ) )))))




(describe "select all entries on today"
    (given [year (. date get 1)
            month (+ 1 (. date get 2)) ; because calender month starts with 0 for january
            day (. date get 5)]
      (with [(before (init-derby))]
           (it  "finds all entries from today" 
              (< 1 (count (find-atom-feed "sso" day month year  )))))))

