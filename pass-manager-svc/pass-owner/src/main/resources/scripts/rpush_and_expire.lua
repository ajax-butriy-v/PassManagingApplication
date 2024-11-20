local key = KEYS[1]
local value = ARGV[1]
redis.call("RPUSH", key, value)
redis.call("EXPIRE", key, tonumber(ARGV[2]))
