import urllib.request
import json

req = urllib.request.Request(
    'https://developer.android.com/training/tv/discover/engage',
    headers={'User-Agent': 'Mozilla/5.0'}
)
try:
    with urllib.request.urlopen(req) as response:
        html = response.read().decode('utf-8')
        import re
        matches = re.findall(r'com\.google\.android\.engage:.*?["\']', html)
        print("Engage Dependencies:", set(matches))
except Exception as e:
    print(e)
