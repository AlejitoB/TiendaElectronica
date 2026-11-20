import http from "node:http";
import { atender } from "./tienda-http.js";

const port = Number(process.env.PORT || 4173);
http.createServer(atender).listen(port, "127.0.0.1", () =>
  console.log("Circuito en http://127.0.0.1:" + port),
);
