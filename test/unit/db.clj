(ns unit.db 
    (:use     derby [feeds.db :only (find-atom-entry) ]
              lazytest.context.stub
              [lazytest.context :only (fn-context)]
              [lazytest.describe :only (describe it given do-it for-any with before)]
              [lazytest.expect :only (expect)]
              [lazytest.expect.thrown :only (throws?)]))

(describe "Init derby base"
            (with [(before (init-derby))]
            (it "initialiser derby database"
              (init-derby))))

(describe "Select atom entries"
              (with [(before (init-derby))]
                  (it "finds one entry with feed = sso and id 1"
                      (= 1 (:id (find-atom-entry "sso" 1 ) )))))

