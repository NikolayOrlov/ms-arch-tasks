
PostgreSQL can be accessed via port 5432 on the following DNS names from within your cluster:

    pgdb-postgresql.database.svc.cluster.local - Read/Write connection

To get the password for "postgres" run:

    export POSTGRES_ADMIN_PASSWORD=$(kubectl get secret --namespace database pgdb-postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)

To get the password for "user" run:

    export POSTGRES_PASSWORD=$(kubectl get secret --namespace database pgdb-postgresql -o jsonpath="{.data.password}" | base64 -d)

To connect to your database run the following command:

    kubectl run pgdb-postgresql-client --rm --tty -i --restart='Never' --namespace database --image docker.io/bitnami/postgresql:16.3.0-debian-12-r14 --env="PGPASSWORD=$POSTGRES_PASSWORD" \
      --command -- psql --host pgdb-postgresql -U user -d service -p 5432

    > NOTE: If you access the container using bash, make sure that you execute "/opt/bitnami/scripts/postgresql/entrypoint.sh /bin/bash" in order to avoid the error "psql: local user with ID 1001} does not exist"

To connect to your database from outside the cluster execute the following commands:

    kubectl port-forward --namespace database svc/pgdb-postgresql 5432:5432 &
    PGPASSWORD="$POSTGRES_PASSWORD" psql --host 127.0.0.1 -U user -d service -p 5432

WARNING: The configured password will be ignored on new installation in case when previous PostgreSQL release was deleted through the helm command. In that case, old PVC will have an old password, and setting it through helm won't take effect. Deleting persistent volumes (PVs) will solve the issue.

WARNING: There are "resources" sections in the chart not set. Using "resourcesPreset" is not recommended for production. For production installations, please set the following values according to your workload needs:
- primary.resources
- readReplicas.resources
  +info https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/

⚠ SECURITY WARNING: Original containers have been substituted. This Helm chart was designed, tested, and validated on multiple platforms using a specific set of Bitnami and Tanzu Application Catalog containers. Substituting other containers is likely to cause degraded security and performance, broken chart features, and missing environment variables.