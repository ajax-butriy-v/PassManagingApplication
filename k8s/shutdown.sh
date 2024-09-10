for file in *.yaml; do
  kubectl delete -f "$file"
done
