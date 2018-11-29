# Sanity check the sota setup for common misconfigurations

def sota_check_overrides(status, d):
    for var in (d.getVar('SOTA_OVERRIDES_BLACKLIST', True) or "").split():
        if var in d.getVar('OVERRIDES', True).split(':'):
            status.addresult("%s should not be a overrides, because it is a image fstype in updater layer, please check your OVERRIDES setting.\n" % var)

def sota_check_required_variables(status, d):
    for var in (d.getVar('SOTA_REQUIRED_VARIABLES', True) or "").split():
        if not d.getVar(var, True):
            status.addresult("%s should be set in your local.conf.\n" % var)

def sota_raise_sanity_error(msg, d):
    if d.getVar("SANITY_USE_EVENTS", True) == "1":
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
