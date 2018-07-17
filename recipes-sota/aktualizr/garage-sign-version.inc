
python () {
    if d.getVar("GARAGE_SIGN_VERSION", True) or not d.getVar("SOTA_PACKED_CREDENTIALS", True):
        return
    import json
    import urllib.request
    import zipfile
    with zipfile.ZipFile(d.getVar("SOTA_PACKED_CREDENTIALS", True), 'r') as zip_ref:
        try:
            with zip_ref.open('tufrepo.url', mode='r') as url_file:
                url = url_file.read().decode().strip(' \t\n') + '/health/version'
        except (KeyError, ValueError, RuntimeError):
            return
    connected = False
    tries = 3
    for i in range(tries):
        try:
            r = urllib.request.urlopen(url)
            if r.code == 200:
                connected = True
                break
            else:
                print('Bad return code from server ' + url + ': ' + str(r.code) +
                      ' (attempt ' + str(i + 1) + ' of ' + str(tries) + ')')
        except urllib.error.URLError as e:
            print('Error connecting to server ' + url + ': ' + str(e) +
                  ' (attempt ' + str(i + 1) + ' of ' + str(tries) + ')')
    if not connected:
        return
    resp = r.read().decode('utf-8')
    j = json.loads(resp)
    version = 'cli-' + j['version'] + '.tgz'
    d.setVar("GARAGE_SIGN_VERSION", version)
}

# vim:set ts=4 sw=4 sts=4 expandtab:
