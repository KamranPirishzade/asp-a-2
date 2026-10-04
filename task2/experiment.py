import sys

tpl = (1, 2, 3)
lst = [1, 2, 3]
print("Experiment 1")
print("tuple (1,2,3):", tpl.__sizeof__())
print("list  [1,2,3]:", lst.__sizeof__())

print("\nExperiment 2")
print("empty tuple:", ().__sizeof__())
print("empty list: ", [].__sizeof__())

print("\nExperiment 3")
for n in range(11):
    t = tuple(range(n))
    print(f"tuple len={n}:", t.__sizeof__())         

print("\nExperiment 4")
lst = []
prev = lst.__sizeof__()
print(f"list len=0:", prev)
for i in range(20):
    lst.append(i)
    size = lst.__sizeof__()                             
    if size != prev:                               
        print(f"list len={len(lst)}:", size)
        prev = size                      


print("\nExperiment 5")
print("tuple: __sizeof__ =", tpl.__sizeof__(), " getsizeof =", sys.getsizeof(tpl))
print("list:  __sizeof__ =", lst.__sizeof__(), " getsizeof =", sys.getsizeof(lst))

print("\nExperiment 6")
print("literal [1,2,3]: ", [1, 2, 3].__sizeof__())
print("list((1,2,3)): ", list((1, 2, 3)).__sizeof__())
print("comprehension: ", [x for x in range(1, 4)].__sizeof__()) 