package com.seyone22.atproto_auth2

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AtProtoAuthTest {

    private lateinit var client: HttpClient

    @BeforeTest
    fun setup() {
        client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { request ->
                    when (request.url.parameters["handle"]) {
                        "madrilenyer.bsky.social" -> {
                            respond(
                                """{"did": "did:plc:123456"}""",
                                HttpStatusCode.OK,
                                headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }

                        else -> {
                            respondError(HttpStatusCode.NotFound) // Simulating an error case
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `test resolveHandle returns correct DID`() = runBlocking {
        val result = fetchDIDFromHandle("madrilenyer.bsky.social")
        assertEquals("did:plc:tjc27aje4uwxtw5ab6wwm4km", result)
    }

    @Test
    fun `test resolveHandle returns null on error`() = runBlocking {
        val result = fetchDIDFromHandle("")
        assertNull(result)
    }

    @Test
    fun `test fetchServiceEndpoint returns correct endpoint`() = runBlocking {
        client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { request ->
                    when (request.url.toString()) {
                        "https://plc.directory/did:plc:tjc27aje4uwxtw5ab6wwm4km" -> {
                            respond(
                                """
                            {
                                "service": [
                                    {
                                        "id": "#atproto_pds",
                                        "type": "AtprotoPersonalDataServer",
                                        "serviceEndpoint": "https://velvetfoot.us-east.host.bsky.network"
                                    }
                                ]
                            }
                            """,
                                HttpStatusCode.OK,
                                headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }
                        else -> {
                            respondError(HttpStatusCode.NotFound)
                        }
                    }
                }
            }
        }

        val result = fetchServiceEndpoint("did:plc:tjc27aje4uwxtw5ab6wwm4km", client)
        assertEquals("https://velvetfoot.us-east.host.bsky.network", result)
    }

    @Test
    fun `test fetchServiceEndpoint returns null when missing service`() = runBlocking {
        client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { request ->
                    respond(
                        """{ "service": [] }""", // Empty service array
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        }

        val result = fetchServiceEndpoint("did:plc:tjc27aje4uwxtw5ab6wwm4km", client)
        assertNull(result)
    }

    @Test
    fun `test fetchServiceEndpoint returns null on error`() = runBlocking {
        client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { request ->
                    respondError(HttpStatusCode.BadRequest) // Simulating an error case
                }
            }
        }

        val result = fetchServiceEndpoint("did:plc:tjc27aje4uwxtw5ab6wwm4km", client)
        assertNull(result)
    }

    @Test
    fun `test fetchAuthorizationServer returns correct server`() = runBlocking {
        client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { request ->
                    when (request.url.toString()) {
                        "https://velvetfoot.us-east.host.bsky.network/.well-known/oauth-protected-resource" -> {
                            respond(
                                """
                            {
                                "resource": "https://velvetfoot.us-east.host.bsky.network",
                                "authorization_servers": ["https://bsky.social"],
                                "scopes_supported": [],
                                "bearer_methods_supported": ["header"],
                                "resource_documentation": "https://atproto.com"
                            }
                            """,
                                HttpStatusCode.OK,
                                headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }
                        else -> {
                            respondError(HttpStatusCode.NotFound)
                        }
                    }
                }
            }
        }

        val result = fetchPDSMetadata("https://velvetfoot.us-east.host.bsky.network", client)
        assertEquals("https://bsky.social", result)
    }

    @Test
    fun `test fetchAuthorizationServer returns null on missing field`() = runBlocking {
        client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { request ->
                    respond(
                        """{ "resource": "https://velvetfoot.us-east.host.bsky.network" }""", // Missing auth_servers
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        }

        val result = fetchPDSMetadata("https://velvetfoot.us-east.host.bsky.network", client)
        assertNull(result)
    }

    @Test
    fun `test fetchAuthorizationServer returns null on error`() = runBlocking {
        client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { request ->
                    respondError(HttpStatusCode.InternalServerError) // Simulating an error
                }
            }
        }

        val result = fetchPDSMetadata("https://velvetfoot.us-east.host.bsky.network", client)
        assertNull(result)
    }

    @Test
    fun `test fetchOAuthServerMetadata returns correct metadata`() = runBlocking {
        client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { request ->
                    when (request.url.toString()) {
                        "https://bsky.social/.well-known/oauth-authorization-server" -> {
                            respond(
                                """
                            {
                                "issuer": "https://bsky.social",
                                "authorization_endpoint": "https://bsky.social/oauth/authorize",
                                "token_endpoint": "https://bsky.social/oauth/token",
                                "pushed_authorization_request_endpoint": "https://bsky.social/oauth/par"
                            }
                            """,
                                HttpStatusCode.OK,
                                headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }
                        else -> {
                            respondError(HttpStatusCode.NotFound)
                        }
                    }
                }
            }
        }

        val result = fetchOAuthServerMetadata("https://bsky.social", client)
        assertEquals("https://bsky.social/oauth/authorize", result?.authorizationEndpoint)
        assertEquals("https://bsky.social/oauth/token", result?.tokenEndpoint)
        assertEquals("https://bsky.social/oauth/par", result?.pushedAuthorizationRequestEndpoint)
    }

    @Test
    fun `test fetchOAuthServerMetadata returns null on error`() = runBlocking {
        client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { request ->
                    respondError(HttpStatusCode.InternalServerError) // Simulating a failure
                }
            }
        }

        val result = fetchOAuthServerMetadata("https://bsky.social", client)
        assertNull(result)
    }


}
