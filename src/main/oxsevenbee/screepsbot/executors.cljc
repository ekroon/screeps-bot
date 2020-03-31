(ns oxsevenbee.screepsbot.executors)

(defprotocol RoomExecutor
  (should-execute [this opts])
  (execute [this opts]))