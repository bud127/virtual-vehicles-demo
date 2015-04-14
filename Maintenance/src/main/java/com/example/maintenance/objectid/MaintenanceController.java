package com.example.maintenance.objectid;

import java.util.List;

import io.netty.handler.codec.http.HttpMethod;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.common.query.QueryFilter;
import org.restexpress.common.query.QueryOrder;
import org.restexpress.common.query.QueryRange;
import org.restexpress.query.QueryFilters;
import org.restexpress.query.QueryOrders;
import org.restexpress.query.QueryRanges;
import com.example.maintenance.Constants;

import com.strategicgains.hyperexpress.HyperExpress;
import com.strategicgains.hyperexpress.builder.TokenBinder;
import com.strategicgains.hyperexpress.builder.TokenResolver;
import com.strategicgains.hyperexpress.builder.UrlBuilder;
import com.strategicgains.repoexpress.mongodb.Identifiers;

/**
 * This is the 'controller' layer, where HTTP details are converted to domain
 * concepts and passed to the maintenance layer. Then maintenance layer response
 * information is enhanced with HTTP details, if applicable, for the response.
 * <p/>
 * This controller demonstrates how to process an entity that is identified by a
 * MongoDB ObjectId.
 */
public class MaintenanceController {

    private static final UrlBuilder LOCATION_BUILDER = new UrlBuilder();
    private MaintenanceService maintenance;

    public MaintenanceController(MaintenanceService maintenanceService) {
        super();
        this.maintenance = maintenanceService;
    }

    public Maintenance create(Request request, Response response) {
        Maintenance entity = request.getBodyAs(Maintenance.class, "Resource details not provided");
        Maintenance saved = maintenance.create(entity);

        // Construct the response for create...
        response.setResponseCreated();

        // Bind the resource with link URL tokens, etc. here...
        TokenResolver resolver = HyperExpress.bind(Constants.Url.MAINTENANCE_ID, Identifiers.MONGOID.format(saved.getId()));

        // Include the Location header...
        String locationPattern = request.getNamedUrl(HttpMethod.GET, Constants.Routes.SINGLE_MAINTENANCE);
        response.addLocationHeader(LOCATION_BUILDER.build(locationPattern, resolver));

        // Return the newly-created resource...
        return saved;
    }

    public Maintenance read(Request request, Response response) {
        String id = request.getHeader(Constants.Url.MAINTENANCE_ID, "No resource ID supplied");
        Maintenance entity = maintenance.read(Identifiers.MONGOID.parse(id));

        // enrich the resource with links, etc. here...
        HyperExpress.bind(Constants.Url.MAINTENANCE_ID, Identifiers.MONGOID.format(entity.getId()));

        return entity;
    }

    public List<Maintenance> readAll(Request request, Response response) {
        QueryFilter filter = QueryFilters.parseFrom(request);
        QueryOrder order = QueryOrders.parseFrom(request);
        QueryRange range = QueryRanges.parseFrom(request, 20);
        List<Maintenance> entities = maintenance.readAll(filter, range, order);
        long count = maintenance.count(filter);
        response.setCollectionResponse(range, entities.size(), count);

        // Bind the resources in the collection with link URL tokens, etc. here...
        HyperExpress.tokenBinder(new TokenBinder<Maintenance>() {
            @Override
            public void bind(Maintenance entity, TokenResolver resolver) {
                resolver.bind(Constants.Url.MAINTENANCE_ID, Identifiers.MONGOID.format(entity.getId()));
            }
        });

        return entities;
    }

    public void update(Request request, Response response) {
        String id = request.getHeader(Constants.Url.MAINTENANCE_ID, "No resource ID supplied");
        Maintenance entity = request.getBodyAs(Maintenance.class, "Resource details not provided");
        entity.setId(Identifiers.MONGOID.parse(id));
        maintenance.update(entity);
        response.setResponseNoContent();
    }

    public void delete(Request request, Response response) {
        String id = request.getHeader(Constants.Url.MAINTENANCE_ID, "No resource ID supplied");
        maintenance.delete(Identifiers.MONGOID.parse(id));
        response.setResponseNoContent();
    }
}