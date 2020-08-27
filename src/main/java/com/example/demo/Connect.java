package com.example.demo;

import static com.datastax.dse.driver.api.core.graph.DseGraph.*;
import com.datastax.dse.driver.api.core.graph.DseGraph;
import com.datastax.dse.driver.api.core.graph.FluentGraphStatement;
import com.datastax.dse.driver.api.core.graph.GraphNode;
import com.datastax.dse.driver.api.core.graph.GraphResultSet;
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
import com.datastax.oss.driver.api.core.CqlSession;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import java.net.InetSocketAddress;
import java.util.List;

public class Connect
{
    public void test ()
    {
        testWithGremlinImplicit_DOESNOTWORK();
        //testWithGremlinImplicit_WORKS();
        //testWithFluent();
        //testWithScriptStatement();
    }

    void testWithGremlinImplicit_DOESNOTWORK ()
    {
        CqlSession session = getSession();

        GraphTraversalSource g = DseGraph.g.withRemote(DseGraph.remoteConnectionBuilder(
            session).build());

        g.V().addV("test2").property("name", "test123").next();

        List<Vertex> vs = g.V().toList();
        for (Vertex v : vs) {
            System.out.println(" v=" + v.toString());
        }

        Long count = g.V().count().next();
        System.out.println("count=" + count);
    }

    void testWithGremlinImplicit_WORKS ()
    {
        CqlSession session = getSession();

        GraphTraversalSource g = DseGraph.g.withRemote(DseGraph.remoteConnectionBuilder(
            session).build());

        g.V().addV("test2").property("name", "test123").next();

        // re-initialize session and traversal source
        session.close();
        session = getSession();
        g = DseGraph.g.withRemote(DseGraph.remoteConnectionBuilder(
            session).build());

        List<Vertex> vs = g.V().toList();
        for (Vertex v : vs) {
            System.out.println(" v=" + v.toString());
        }

        // re-initialize session and traversal source
        session.close();
        session = getSession();
        g = DseGraph.g.withRemote(DseGraph.remoteConnectionBuilder(
            session).build());

        Long count = g.V().count().next();
        System.out.println("count=" + count);
    }

    void testWithFluent ()
    {
        try (CqlSession session = getSession()) {

            GraphResultSet result = session.execute(FluentGraphStatement.newInstance(
                g.addV("test2").property("name", "testzzz1")));
            for (GraphNode node : result) {
                System.out.println("vertex=" + node.asVertex());
            }

            GraphResultSet grs = session.execute(FluentGraphStatement.newInstance(g.V()));
            List<GraphNode> vs = grs.all();
            for (GraphNode v : vs) {
                System.out.println(" v=" + v.toString());
            }

            GraphResultSet grs1 = session.execute(FluentGraphStatement.newInstance(g.V().count()));
            Long count = grs1.one().asLong();
            System.out.println("count=" + count);
        }
    }

    void testWithScriptStatement ()
    {
        try (CqlSession session = getSession()) {

            session.execute(ScriptGraphStatement.newInstance(
                "g.addV(vertexLabel)").setQueryParam("vertexLabel", "test_vertex_2"));

            GraphResultSet grs = session.execute(ScriptGraphStatement.newInstance("g.V()"));
            List<GraphNode> vs = grs.all();
            for (GraphNode v : vs) {
                System.out.println(" v=" + v.toString());
            }

            GraphResultSet grs1 = session.execute(ScriptGraphStatement.newInstance(
                "g.V().count()"));
            Long count = grs1.one().asLong();
            System.out.println("count=" + count);
        }
    }

    CqlSession getSession ()
    {
        return CqlSession.builder()
            .addContactPoint(new InetSocketAddress("10.169.48.148", 9042))
            .withAuthCredentials("dmsuser", "dmsuser123")
            .withLocalDatacenter("dc1")
            .build();
    }
}
