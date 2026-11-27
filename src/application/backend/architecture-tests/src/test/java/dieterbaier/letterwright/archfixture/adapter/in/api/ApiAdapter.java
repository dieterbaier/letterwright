package dieterbaier.letterwright.archfixture.adapter.in.api;

import dieterbaier.letterwright.archfixture.api.ApiRecord;
import dieterbaier.letterwright.archfixture.api.ApiInterface;
import dieterbaier.letterwright.archfixture.usecases.in.InboundPort;

/** Fixture: compliant, adapter-api implements the API and calls an inbound port. */
public class ApiAdapter implements ApiInterface {

    private InboundPort useCase;

    @Override
    public ApiRecord save(String user) {
        return new ApiRecord(user);
    }
}
