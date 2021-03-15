(ns tgn-bot.events
  (:require [tgn-bot.core :refer [state config bot-id]]
            [tgn-bot.acceptance :refer [accept]]
            [tgn-bot.commands :refer [handle-command]]
            [discljord.messaging :as messaging]
            [discljord.formatting :as formatting]))

(defmulti handle-event (fn [type data] type))

(defmethod handle-event :default [type data]
  #_(println type data))

(defn introduction-message [user]
  (format
   (get-in config [:messages :introduction])
   (formatting/mention-user user)
   (formatting/mention-channel (get-in config [:channel-ids :introduction]))))

(defmethod handle-event :guild-member-add
  [_ {:keys [user] :as data}]
  (messaging/create-message! (:rest @state) (get-in config [:channel-ids :introduction]) :content (introduction-message user)))

(defn random-response [user]
  (str (rand-nth (:responses config)) ", " (formatting/mention-user user) \!))

(def command-pattern (re-pattern (str (:command-prefix config) #"(\S+)\s*(.*)")))

(defmethod handle-event :message-create
  [_ {:keys [channel-id author content mentions] :as data}]
  (when (not= (:id author) @bot-id)
    (when (some #{@bot-id} (map :id mentions))
      (messaging/create-message! (:rest @state) channel-id :content (str (random-response author) " ")))
    (when-let [[_ command args] (re-matches command-pattern content)]
      (handle-command (keyword command) args data))))