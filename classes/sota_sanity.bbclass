# Sanity check the sota setup for common misconfigurations

def sota_check_overrides(status, d):
    for var in (d.getVar('SOTA_OVERRIDES_BLACKLIST') or "").split():
        if var in d.getVar('OVERRIDES').split(':'):
            status.addresult("%s should not be a overrides, because it is a image fstype in updater layer, please check your OVERRIDES setting.\n" % var)

def sota_check_required_variables(status, d):
    for var in (d.getVar('SOTA_REQUIRED_VARIABLES') or "").split():
        if not d.getVar(var):
            status.addresult("%s should be set in your local.conf.\n" % var)

def sota_check_variables_validity(status, d):
    import re
    import os.path

    if d.getVar("OSTREE_BRANCHNAME") and re.match("^[a-zA-Z0-9_-]*$", d.getVar("OSTREE_BRANCHNAME")) is None:
        status.addresult("OSTREE_BRANCHNAME Should only contain characters from the character set [a-zA-Z0-9_-].\n")
    if d.getVar("SOTA_HARDWARE_ID") and re.match("^[a-zA-Z0-9_-]*$", d.getVar("SOTA_HARDWARE_ID")) is None:
        status.addresult("SOTA_HARDWARE_ID Should only contain characters from the character set [a-zA-Z0-9_-].\n")
    if d.getVar("SOTA_CLIENT_FEATURES") is not None:
        for feat in d.getVar("SOTA_CLIENT_FEATURES").split(' '):
            if feat not in ("hsm", "serialcan", "ubootenv", ""):
                status.addresult("SOTA_CLIENT_FEATURES should only include hsm, serialcan and bootenv.\n")
                break
    if d.getVar("SOTA_CLIENT_PROV") is not None:
        prov = d.getVar("SOTA_CLIENT_PROV").strip()
        if prov not in ("aktualizr-shared-prov", "aktualizr-device-prov", "aktualizr-device-prov-hsm", ""):
            status.addresult("Valid options for SOTA_CLIENT_PROV are aktualizr-shared-prov, aktualizr-device-prov and aktualizr-device-prov-hsm.\n")
            if prov == "aktualizr-auto-prov":
                bb.warn('aktualizr-auto-prov is deprecated. Please use aktualizr-shared-prov instead.')
            elif prov == "aktualizr-ca-implicit-prov":
                bb.warn('aktualizr-ca-implicit-prov is deprecated. Please use aktualizr-device-prov instead.')
            elif prov == "aktualizr-hsm-prov":
                bb.warn('aktualizr-hsm-prov is deprecated. Please use aktualizr-device-prov-hsm instead.')
    if d.getVar("GARAGE_TARGET_URL") and re.match("^(https?|ftp|file)://.+$", d.getVar("GARAGE_TARGET_URL")) is None:
        status.addresult("GARAGE_TARGET_URL is set to a bad url.\n")
    if d.getVar("SOTA_POLLING_SEC") and re.match("^[1-9]\d*|0$", d.getVar("SOTA_POLLING_SEC")) is None:
        status.addresult("SOTA_POLLING_SEC should be an integer.\n")
    config = d.getVar("SOTA_SECONDARY_CONFIG")
    if config is not None and config != "":
        path = os.path.abspath(config)
        if not os.path.exists(path):
            status.addresult("SOTA_SECONDARY_CONFIG is not set correctly. The file containing JSON configuration for secondaries does not exist.\n")
    credentials = d.getVar("SOTA_PACKED_CREDENTIALS")
    if credentials is not None and credentials != "":
        path = os.path.abspath(credentials)
        if not os.path.exists(path):
            status.addresult("SOTA_PACKED_CREDENTIALS is not set correctly. The zipped credentials file does not exist.\n")
    if d.getVar("OSTREE_UPDATE_SUMMARY") and d.getVar("OSTREE_UPDATE_SUMMARY") not in ("0", "1", ""):
        status.addresult("OSTREE_UPDATE_SUMMARY should be set to 0 or 1.\n")
    if d.getVar("OSTREE_DEPLOY_DEVICETREE") and d.getVar("OSTREE_DEPLOY_DEVICETREE") not in ("0", "1", ""):
        status.addresult("OSTREE_DEPLOY_DEVICETREE should be set to 0 or 1.\n")
    if d.getVar("GARAGE_SIGN_AUTOVERSION") and d.getVar("GARAGE_SIGN_AUTOVERSION") not in ("0", "1", ""):
        status.addresult("GARAGE_SIGN_AUTOVERSION should be set to 0 or 1.\n")
    if d.getVar("SOTA_DEPLOY_CREDENTIALS") and d.getVar("SOTA_DEPLOY_CREDENTIALS") not in ("0", "1", ""):
        status.addresult("SOTA_DEPLOY_CREDENTIALS should be set to 0 or 1.\n")
    
def sota_raise_sanity_error(msg, d):
    if d.getVar("SANITY_USE_EVENTS") == "1":
        bb.event.fire(bb.event.SanityCheckFailed(msg), d)
        return

    bb.fatal("Sota's config sanity checker detected a potential misconfiguration.\n"
             "Please fix the cause of this error then you can continue to build.\n"
             "Following is the list of potential problems / advisories:\n"
             "\n%s" % msg)

def sota_check_sanity(sanity_data):
    class SanityStatus(object):
        def __init__(self):
            self.messages = ""
            self.reparse = False

        def addresult(self, message):
            if message:
                self.messages = self.messages + message

    status = SanityStatus()

    sota_check_overrides(status, sanity_data)
    sota_check_required_variables(status, sanity_data)
    sota_check_variables_validity(status, sanity_data)

    if status.messages != "":
        sota_raise_sanity_error(sanity_data.expand(status.messages), sanity_data)

addhandler sota_check_sanity_eventhandler
sota_check_sanity_eventhandler[eventmask] = "bb.event.SanityCheck"

python sota_check_sanity_eventhandler() {
    if bb.event.getName(e) == "SanityCheck":
        sanity_data = copy_data(e)
        if e.generateevents:
            sanity_data.setVar("SANITY_USE_EVENTS", "1")
        reparse = sota_check_sanity(sanity_data)
        e.data.setVar("BB_INVALIDCONF", reparse)
        bb.event.fire(bb.event.SanityCheckPassed(), e.data)

    return
}
