class UrlMappings {

    static mappings = {
        "/admin/$controller/$action?/$id?"(namespace : "admin")

        "/$controller/$action?/$id?"()

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
