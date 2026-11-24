package dieterbaier.standardschreiben.archfixture.adapter.in.api;

import dieterbaier.standardschreiben.archfixture.api.ApiDatensatz;
import dieterbaier.standardschreiben.archfixture.api.ApiSchnittstelle;
import dieterbaier.standardschreiben.archfixture.usecases.in.EingehenderPort;

/** Fixture: regelkonform, adapter-api implementiert die API und ruft einen eingehenden Port. */
public class ApiAdapter implements ApiSchnittstelle {

    private EingehenderPort anwendungsfall;

    @Override
    public ApiDatensatz speichere(String benutzer) {
        return new ApiDatensatz(benutzer);
    }
}
