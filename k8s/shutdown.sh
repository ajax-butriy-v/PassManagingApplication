kubectl delete -f app.yaml \
               -f app-configmap.yaml \
               -f app-ingress.yaml \
               -f gateway.yaml \
               -f gateway-ingress.yaml \
               -f mongo.yaml \
               -f mongo-secret.yaml \
               -f nats.yaml \
