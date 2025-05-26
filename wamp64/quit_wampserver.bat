echo off
NET STOP wampapache64
NET STOP wampmysqld64
NET STOP wampmariadb64
wampmanager.exe -quit -id={wampserver}