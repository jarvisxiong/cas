package com.inmobi.adserve.channels.adnetworks.ix;

import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;

import com.google.gson.Gson;

/**
 * @author ritwik.kumar
 */
public class PersonThriftTest {
    private static final Gson GSON = new Gson();

    private static Person getPersonPefect() {
        final Person p = new Person("firstName", "lastName", 9964, 123);
        p.setSecondryPhone(456);
        p.setAddress("HSR");
        return p;
    }

    private static Person getPersonNameLess() {
        final Person p = new Person();
        p.setFirstName("firstName");
        p.setMobile(9964);
        p.setId(123);
        p.setAddress("HSR");
        p.setSecondryPhone(456);
        return p;
    }

    private static Person getPersonIdLess() {
        final Person p = new Person();
        p.setFirstName("firstName");
        p.setLastName("lastName");
        p.setMobile(9964);
        p.setAddress("HSR");
        p.setSecondryPhone(456);
        return p;
    }

    private static String getJSON(final Person person) {
        try {
            final TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
            return serializer.toString(person);
        } catch (TException e) {
            System.err.println("getJSON ->" + e.getMessage());
        }
        return null;
    }

    private static void printJSON(final Person person) {
        final String json = getJSON(person);
        if (json != null) {
            System.out.println(json);
        }
    }

    private static void validate(final Person person) {
        try {
            person.validate();
        } catch (TException e) {
            System.err.println("validate ->" + e.getMessage());
        }
    }

    private static Person getPersonInCarnated(final String json) {
        return GSON.fromJson(json, Person.class);
    }

    private static void applyStuffs(final Person person) {
        System.out.println(person);
        printJSON(person);
        validate(person);
    }


    public static void main(final String[] args) throws Exception {
        final Person personPefect = getPersonPefect();
        final Person personNameLess = getPersonNameLess();
        final Person personIdLess = getPersonIdLess();

        applyStuffs(personPefect);
        applyStuffs(personNameLess);
        applyStuffs(personIdLess);

        System.out.println("=============Incarnation=============");
        final Person personPefectInc =
                getPersonInCarnated("{\"firstName\":\"firstName\",\"lastName\":\"lastName\",\"address\":\"HSR\",\"mobile\":9964,\"id\":123,\"secondryPhone\":456}");
        applyStuffs(personPefectInc);

        final Person personNameLessInc =
                getPersonInCarnated("{\"firstName\":\"firstName\",\"address\":\"HSR\",\"mobile\":9964,\"id\":123,\"secondryPhone\":456}");
        applyStuffs(personNameLessInc);

        final Person personIdLessInc =
                getPersonInCarnated("{\"firstName\":\"firstName\",\"lastName\":\"lastName\",\"address\":\"HSR\",\"mobile\":9964,\"secondryPhone\":456}");
        applyStuffs(personIdLessInc);

    }

}
