from django.db import models

class ModelA(models.Model):
    integer_field = models.IntegerField()
    decimal_field = models.DecimalField(max_digits=10, decimal_places=4)
    duration_field = models.DurationField()
    file_field = models.FileField()
    float_field = models.FloatField()
    boolean_field = models.BooleanField()
    json_field = models.JSONField()
    uuid_field = models.UUIDField()
    char_field = models.CharField(max_length=200)
    date_field = models.DateField(auto_now=True)
    time_field = models.TimeField(auto_now=True)
    date_time_field = models.DateTimeField(auto_now=True)
    foreign_key_recursive_field = models.ForeignKey('ModelA', blank=True, null=True, related_name='reverse_field', on_delete=models.SET_NULL)
    foreign_key_field = models.ForeignKey('ModelB', blank=True, null=True, related_name='reverse_field', on_delete=models.SET_NULL)
    many_recursive_field = models.ManyToManyField('ModelA', blank=True, related_name='reverse_many_recursive_field')
    many_field = models.ManyToManyField('ModelB', blank=True, related_name='reverse_many_field')

    def __str__(self):
        return self.char_field

class ModelB(models.Model):
    name = models.CharField(max_length=200)

    def __str__(self):
        return self.name