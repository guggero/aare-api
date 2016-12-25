package ch.illubits.api.boundary;

import ch.illubits.api.control.MeasurementRepository;
import ch.illubits.api.entity.Measurement;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Stateless
@Path("v1")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class V1Resource {

    @Inject
    private MeasurementRepository measurementRepository;

    @GET
    @Path("/current")
    public Response getCurrentMeasurements() {
        List<Measurement> measurements = measurementRepository.findAll();

        return Response.ok(measurements).build();
    }
}
