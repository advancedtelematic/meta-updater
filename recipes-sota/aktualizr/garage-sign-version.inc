
python () {
    if d.getVar("GARAGE_SIGN_VERSION", True) or not d.getVar("SOTA_PACKED_CREDENTIALS", True):
        return
    import json
    import urllib.request
    import zipfile
    with zipfile.ZipFile(d.getVar("SOTA_PACKED_CREDENTIALS", True), 'r') as zip_ref:
        try:
            with zip_ref.open('tufrepo.url', mode='r') as url_file:
                url = url_file.read().decode() + '/health/version'
        except (KeyError, ValueError, RuntimeError):
            return
    r = urllib.request.urlopen(url)
    if r.code != 200:
        return
    resp = r.read().decode('utf-8')
    j = json.loads(resp)
    version = 'cli-' + j['version'] + '.tgz'
    d.setVar("GARAGE_SIGN_VERSION", version)
}

# vim:set ts=4 sw=4 sts=4 expandtab:
