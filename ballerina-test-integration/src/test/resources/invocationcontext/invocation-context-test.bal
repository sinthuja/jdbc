import ballerina/http;
import ballerina/log;
import ballerina/runtime;
import ballerina/io;

@final string attributeName = "attribute";
@final string attributeValue = "value";

public type Filter1 object {
    public function filterRequest (http:Request request, http:FilterContext context) returns http:FilterResult {
        log:printInfo("Add attribute to invocation context from filter");
        runtime:getInvocationContext().attributes[attributeName] = attributeValue;
        http:FilterResult filterResponse = {canProceed:true, statusCode:200, message:"successful"};
        return filterResponse;
    }
};

Filter1 filter1;

endpoint http:Listener echoEP {
    port:9090,
    filters:[filter1]
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind echoEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        if (attributeValue == runtime:getInvocationContext().attributes[attributeName]) {
            _ = caller->respond(res);
        } else {
            res.statusCode = 500;
            _ = caller->respond(res);
        }
    }
}

