eval $(minikube docker-env)

docker build --target pass-manager-svc-builder -t pass-manager-svc -f ../Dockerfile ../
docker build --target gateway-builder -t gateway -f ../Dockerfile ../

minikube addons enable ingress

kubectl apply -f mongo-secret.yaml \
              -f app-configmap.yaml \
              -f kafka-configmap.yaml \
              -f zookeeper-configmap.yaml \
              -f mongo.yaml \
              -f mongo-volume.yaml \
              -f mongo-pvc.yaml \
              -f nats.yaml \
              -f zookeeper.yaml \
              -f kafka.yaml \


kubectl wait --for=condition=ready pod -l app=mongodb --timeout=300s
kubectl wait --for=condition=ready pod -l app=kafka --timeout=300s

kubectl apply -f app.yaml \
              -f app-ingress.yaml \
              -f gateway.yaml \
              -f gateway-ingress.yaml
