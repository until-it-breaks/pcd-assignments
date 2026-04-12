## Running JPF Tests

To run the Java Pathfinder (JPF) tests, you must first clone the **jpf-core** repository and use the provided Docker environment.

### 1. Build and start the JPF environment

```bash
docker compose build
docker compose run --rm -v PATH_TO/assignment-01:/home/jpf-core/assignment-01 jpf-dev
```

> Replace `PATH_TO/assignment-01` with the absolute path to your project directory.

---

### 2. Build JPF

Inside the container:

```bash
./gradlew clean build
```

---

### 3. Compile the project sources

```bash
javac -d assignment-01/target/classes \
      -classpath "assignment-01/lib/jpf.jar:assignment-01/target/classes" \
      $(find assignment-01/src/main/java assignment-01/src/test/java -name "*.java")
```

---

### 4. Run JPF tests

Execute the following commands to run each test:

```bash
java -jar build/RunJPF.jar assignment-01/src/test/java/pcd/poool/jpf/TestThreadedCollisionResolver.jpf

java -jar build/RunJPF.jar assignment-01/src/test/java/pcd/poool/jpf/TestUnsafeThreadedCollisionResolver.jpf

java -jar build/RunJPF.jar assignment-01/src/test/java/pcd/poool/jpf/TestPooledCollisionResolver.jpf

java -jar build/RunJPF.jar assignment-01/src/test/java/pcd/poool/jpf/TestThreadedLockFreeCollisionResolver.jpf

java -jar build/RunJPF.jar assignment-01/src/test/java/pcd/poool/jpf/TestPooledLockFreeCollisionResolver.jpf
```

---