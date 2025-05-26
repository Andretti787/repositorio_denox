NET STOP wampapache64
sc delete wampapache64
NET STOP wampmysqld64
sc delete wampmysqld64
NET STOP wampmariadb64
sc delete wampmariadb64
wampmanager.exe -quit -id={wampserver}