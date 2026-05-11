# - - Ejercicio 1
print("--------------------------")
x = 5

def f(x):
    x = x + 2
    print(x)

f(x)
print(x)


# - - Ejercicio 2
print("--------------------------")
def f(x, y=3):
    print(x - y)

f(8)
f(8, 1)


# - - Ejercicio 3
print("--------------------------")
def f(d):
    d["a"] = d["a"] + 1
    print(d["a"])

d = {"a": 2, "b": 5}
f(d)
print(d["a"])


# - - Ejercicio 4
print("--------------------------")
def f(d):
    d["a"].append(4)
    print(d["a"])

x = {"a": [1, 2], "b": [3]}
f(x)
print(x["a"])


# - - Ejercicio 5
print("--------------------------")
def f(d):
    d = {"a": d["a"] + [9]}
    print(d["a"])

x = {"a": [1, 2]}
f(x)
print(x["a"])


# - - Ejercicio 6
print("--------------------------")
x = 10

def f():
    x = 3
    print(x)

def g():
    print(x)

f()
g()
print(x)


# - - Ejercicio 7
print("--------------------------")
x = 1

def f(a):
    global x
    if a in x:
        x[a] = x[a] + 1
    else:
        x[a] = 1
    print(x[a])

x = {"p": 2}
f("p")
f("q")
print(x)


# - - Ejercicio 8
print("--------------------------")
def f(v, p=0):
    v[p] = v[p] + 2
    print(v[p])
    return v[p] - 1

def g(v):
    print(f(v, 1))
    print(f(v))

a = [3, 4, 5]
g(a)
print(a)


# - - Ejercicio 9
print("--------------------------")
def f(d, k):
    d[k].append(len(d[k]))
    print(d[k][-1])
    return d[k][0]

def g(d):
    x = 0
    for k in d:
        if len(d[k]) % 2 == 0:
            x = x + f(d, k)
        else:
            x = x + len(d[k])
    print(x)

a = {"x": [1, 2], "y": [3], "z": [4, 5]}
g(a)
print(a)


# - - Ejercicio 10
print("--------------------------")
def f(a):
    global x
    if a % 2 == 0:
        x = x + a
    else:
        x = x * a
    return x

def g(d):
    for x in d:
        if len(d[x]) > 1:
            print(f(d[x][0]))
        else:
            print(d[x][0])

x = 2
a = {"p": [1], "q": [3, 4], "r": [2, 5]}
g(a)
print(x)


# - - Ejercicio 11
print("--------------------------")
def f(d, k):
    if k in d:
        if len(d[k]) > 1:
            d[k][0] = d[k][0] + d[k][1]
        else:
            d[k].append(0)
        print(d[k][0])
        return len(d[k])
    return 0

def g(d):
    x = 0
    for k in d:
        if k % 2 == 0:
            x = x + f(d, k)
        else:
            x = x + len(d[k])
    print(x)

a = {
    0: [2, 3],
    1: [4],
    2: [1],
    3: [5, 6]
}
g(a)
print(a)


# - - Ejercicio 12
print("--------------------------")

def f(d, k):
    d[k][0] = d[k][0] + len(d[k])
    print(d[k][0])
    return d[k][-1]

def g(d):
    global x
    y = 0
    for k in d:
        if d[k][0] % 2 == 0:
            y = y + f(d, k)
        else:
            d[k].append(y)
            print(d[k][-1])
            y = y + len(d[k])
    x = d
    return y

x = {"a": [2, 1], "b": [3, 4], "c": [4]}
print(g(x))
print(x)
