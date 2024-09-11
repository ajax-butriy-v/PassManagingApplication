eval $(minikube docker-env)
docker build -t passmanagingapplication-app -f ~/IdeaProjects/PassManagingApplication/Dockerfile ~/IdeaProjects/PassManagingApplication

minikube addons enable ingress


kubectl apply -f mongo-secret.yaml
kubectl apply -f mongo-express-configmap.yaml
kubectl apply -f app-configmap.yaml
kubectl apply -f mongo.yaml
kubectl apply -f mongo-express.yaml

kubectl wait --for=condition=ready pod -l app=mongodb --timeout=300s

kubectl apply -f app.yaml
kubectl apply -f app-ingress.yaml
